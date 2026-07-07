import os
from dataclasses import dataclass, field


@dataclass
class KafkaConfig:
    bootstrap_servers: str = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
    requests_topic: str = os.getenv("KAFKA_REQUESTS_TOPIC", "ocarina.download.requests")
    consumer_group: str = os.getenv("KAFKA_CONSUMER_GROUP", "ocarina-py-workers")


@dataclass
class AuthConfig:
    enabled: bool = os.getenv("OCARINA_AUTH_ENABLED", "true").lower() == "true"
    validate_url: str = os.getenv("SHAULA_INTERNAL_VALIDATE_URL", "http://shaula-fastapi:8000/internal/validate")


@dataclass
class KafraConfig:
    url: str = os.getenv("KAFRA_API_URL", "http://kafra-app:3000/api")
    upload_folder: str = os.getenv("KAFRA_UPLOAD_FOLDER", "ocarina_output")


@dataclass
class Config:
    server_port: int = int(os.getenv("SERVER_PORT", "8081"))
    downloads_dir: str = os.getenv("OCARINA_DOWNLOADS_DIR", "./downloads")
    yt_dlp_cookies_file: str = os.getenv("OCARINA_YTDLP_COOKIES_FILE", "")
    cors_allowed_origins: list[str] = field(default_factory=lambda: os.getenv(
        "OCARINA_CORS_ALLOWED_ORIGINS", "http://localhost:5173,http://localhost:8080"
    ).split(","))
    kafka: KafkaConfig = field(default_factory=KafkaConfig)
    auth: AuthConfig = field(default_factory=AuthConfig)
    kafra: KafraConfig = field(default_factory=KafraConfig)


config = Config()
