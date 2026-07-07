from dataclasses import dataclass, field
from datetime import datetime, timezone
from enum import Enum
from uuid import uuid4


class JobStatus(str, Enum):
    QUEUED = "QUEUED"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


class DownloadFormat(str, Enum):
    MP4 = "MP4"
    MP3 = "MP3"


@dataclass
class DownloadJob:
    url: str
    format: DownloadFormat
    id: str = field(default_factory=lambda: uuid4().hex[:12])
    status: JobStatus = JobStatus.QUEUED
    file_name: str | None = None
    error: str | None = None
    save_to_kafra: bool = False
    kafra_path: str | None = None
    created_at: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    updated_at: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "url": self.url,
            "format": self.format.value,
            "status": self.status.value,
            "fileName": self.file_name,
            "error": self.error,
            "saveToKafra": self.save_to_kafra,
            "kafraPath": self.kafra_path,
            "createdAt": self.created_at,
            "updatedAt": self.updated_at,
        }


@dataclass
class DownloadRequest:
    url: str
    format: DownloadFormat = DownloadFormat.MP4
    save_to_kafra: bool = False


@dataclass
class DownloadRequestedEvent:
    job_id: str
    url: str
    format: str
    save_to_kafra: bool = False
    shaula_cookie: str = ""
