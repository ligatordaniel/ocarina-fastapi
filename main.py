import asyncio
import json
import threading
from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse, FileResponse
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path

from config import config
from models import DownloadJob, DownloadRequest, JobStatus, DownloadFormat
from repository import repository
from kafka_producer import send_download_request


def _run_worker():
    """Run Kafka consumer in a background thread."""
    from kafka import KafkaConsumer
    from downloader import downloader
    from kafra_uploader import upload_to_kafra

    print(f"Worker connecting to Kafka: {config.kafka.bootstrap_servers}")
    consumer = KafkaConsumer(
        bootstrap_servers=config.kafka.bootstrap_servers,
        group_id=config.kafka.consumer_group,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        key_deserializer=lambda k: k.decode("utf-8") if k else None,
    )
    consumer.subscribe([config.kafka.requests_topic])
    print(f"Worker listening on topic: {config.kafka.requests_topic}")

    import time
    while True:
        try:
            records = consumer.poll(timeout_ms=5000)
            for tp, msgs in records.items():
                for msg in msgs:
                    event = msg.value
                    job_id = event["jobId"]
                    url = event["url"]
                    format_str = event["format"]
                    save_to_kafra = event.get("saveToKafra", False)
                    shaula_cookie = event.get("shaulaCookie", "")

                    job = repository.find_by_id(job_id)
                    if job is None:
                        continue

                    try:
                        job.status = JobStatus.PROCESSING
                        fmt = DownloadFormat(format_str)
                        file_path = downloader.download(job_id, url, fmt)
                        job.file_name = file_path.name
                        job.status = JobStatus.COMPLETED
                        print(f"Job {job_id} completed: {job.file_name}")

                        # Auto-upload to Kafra if requested
                        if save_to_kafra:
                            print(f"Job {job_id}: uploading to Kafra...")
                            kafra_path = upload_to_kafra(file_path, shaula_cookie)
                            if kafra_path:
                                job.kafra_path = kafra_path
                                print(f"Job {job_id}: saved to Kafra at {kafra_path}")
                            else:
                                print(f"Job {job_id}: Kafra upload failed (file still available locally)")
                    except Exception as e:
                        job.error = str(e)
                        job.status = JobStatus.FAILED
                        print(f"Job {job_id} failed: {e}")
        except Exception as e:
            print(f"Worker poll error: {e}")
            time.sleep(5)


@asynccontextmanager
async def lifespan(app: FastAPI):
    worker_thread = threading.Thread(target=_run_worker, daemon=True)
    worker_thread.start()
    yield


app = FastAPI(title="Ocarina", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=config.cors_allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Shaula auth middleware ──────────────────────────────────────────
@app.middleware("http")
async def shaula_auth(request: Request, call_next):
    if not config.auth.enabled:
        return await call_next(request)

    # skip health endpoint
    if request.url.path in ("/health", "/actuator/health"):
        return await call_next(request)

    cookie = request.cookies.get("shaula_session")
    if not cookie:
        return JSONResponse(
            status_code=401,
            content={"error": "No autenticado. Login requerido en Shaula."},
        )

    try:
        async with httpx.AsyncClient(timeout=5) as client:
            resp = await client.get(
                config.auth.validate_url,
                cookies={"shaula_session": cookie},
            )
            if resp.status_code != 200:
                return JSONResponse(
                    status_code=401,
                    content={"error": "Sesión inválida o expirada."},
                )
    except Exception:
        return JSONResponse(
            status_code=503,
            content={"error": "Servicio de autenticación no disponible."},
        )

    return await call_next(request)


# ── Health ──────────────────────────────────────────────────────────
@app.get("/health")
@app.get("/actuator/health")
def health():
    return {"status": "UP"}


# ── API /api/downloads ──────────────────────────────────────────────
@app.post("/api/downloads", status_code=202)
async def create_download(body: DownloadRequest, request: Request):
    url = body.url.strip()
    if not url:
        raise HTTPException(status_code=400, detail="URL requerida")

    try:
        from urllib.parse import urlparse
        parsed = urlparse(url)
        if parsed.scheme not in ("http", "https"):
            raise HTTPException(400, "URL no permitida. Usa youtube.com o youtu.be")
        host = (parsed.hostname or "").lower()
        if host not in ("youtu.be", "youtube.com") and not host.endswith(".youtube.com"):
            raise HTTPException(400, "URL no permitida. Usa youtube.com o youtu.be")
    except HTTPException:
        raise
    except Exception:
        raise HTTPException(400, "URL inválida")

    shaula_cookie = request.cookies.get("shaula_session", "")
    job = DownloadJob(url=url, format=body.format, save_to_kafra=body.save_to_kafra)
    repository.save(job)
    send_download_request(job, shaula_cookie=shaula_cookie)
    return job.to_dict()


@app.get("/api/downloads")
def list_downloads():
    jobs = repository.find_all()
    jobs.sort(key=lambda j: j.created_at, reverse=True)
    return [j.to_dict() for j in jobs]


@app.get("/api/downloads/{job_id}")
def get_download(job_id: str):
    job = repository.find_by_id(job_id)
    if not job:
        raise HTTPException(status_code=404, detail=f"Job {job_id} no encontrado")
    return job.to_dict()


@app.get("/api/downloads/{job_id}/file")
def download_file(job_id: str):
    job = repository.find_by_id(job_id)
    if not job:
        raise HTTPException(status_code=404, detail=f"Job {job_id} no encontrado")
    if job.status != JobStatus.COMPLETED or not job.file_name:
        raise HTTPException(status_code=409, detail="Descarga no completada aún")

    downloads_dir = Path(config.downloads_dir).resolve()
    file_path = (downloads_dir / job.file_name).resolve()
    if not str(file_path).startswith(str(downloads_dir)) or not file_path.exists():
        raise HTTPException(status_code=404, detail="Archivo no encontrado")

    media_type = "audio/mpeg" if job.format == DownloadFormat.MP3 else "video/mp4"
    return FileResponse(
        path=str(file_path),
        media_type=media_type,
        filename=job.file_name,
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=config.server_port)
