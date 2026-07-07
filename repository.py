import threading
from models import DownloadJob, JobStatus


class JobRepository:
    def __init__(self):
        self._jobs: dict[str, DownloadJob] = {}
        self._lock = threading.Lock()

    def save(self, job: DownloadJob) -> DownloadJob:
        with self._lock:
            self._jobs[job.id] = job
        return job

    def find_by_id(self, job_id: str) -> DownloadJob | None:
        with self._lock:
            return self._jobs.get(job_id)

    def find_all(self) -> list[DownloadJob]:
        with self._lock:
            return list(self._jobs.values())


repository = JobRepository()
