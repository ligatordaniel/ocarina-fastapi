import httpx
from pathlib import Path

from config import config


def upload_to_kafra(file_path: Path, shaula_cookie: str, folder: str = "") -> str | None:
    """Upload a file to Kafra storage. Returns the kafra path on success, None on failure."""
    if not file_path.exists():
        print(f"Kafra upload: file not found {file_path}")
        return None

    base = config.kafra.url.rstrip("/")
    folder = folder or config.kafra.upload_folder
    upload_path = f"{folder}/{file_path.name}" if folder else file_path.name

    url = f"{base}/storage/upload?path={upload_path}"

    try:
        with open(file_path, "rb") as f:
            files = {"file": (file_path.name, f, "application/octet-stream")}
            cookies = {"shaula_session": shaula_cookie} if shaula_cookie else {}
            resp = httpx.put(url, files=files, cookies=cookies, timeout=120)

        if resp.status_code == 200:
            data = resp.json()
            if data.get("ok"):
                print(f"Kafra upload OK: {upload_path}")
                return upload_path
            print(f"Kafra upload failed: {data.get('error', 'unknown')}")
        else:
            print(f"Kafra upload HTTP {resp.status_code}: {resp.text[:200]}")
    except Exception as e:
        print(f"Kafra upload error: {e}")

    return None
