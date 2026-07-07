# ocarina-fastapi

Backend Python FastAPI para recibir un link de YouTube y generar descarga `.mp4` o `.mp3`.

Uso esperado: solo contenido propio o con permiso.

## API

- `POST /api/downloads`

```json
{
  "url": "https://www.youtube.com/watch?v=...",
  "format": "MP4"
}
```

`format`: `MP4` o `MP3`.

- `GET /api/downloads` lista jobs.
- `GET /api/downloads/{id}` estado de un job.
- `GET /api/downloads/{id}/file` descarga el archivo listo.

## Variables

- `SERVER_PORT=8081`
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `OCARINA_DOWNLOADS_DIR=./downloads`
- `OCARINA_YTDLP_COOKIES_FILE=` opcional. Si YouTube pide login/bot check, exporta cookies a `cookies.txt` y móntalo.
- `OCARINA_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080`

## Local con Docker Compose

Desde `/root/jarvis-vite`:

```bash
docker compose up --build
```

Servicios:
- Front: http://localhost:5173
- Backend: http://localhost:8081
- Kafka: `kafka:9092` dentro de Docker.
