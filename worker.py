import json
import signal
import sys
from kafka import KafkaConsumer

from config import config
from models import JobStatus, DownloadFormat
from repository import repository
from downloader import downloader


def main():
    consumer = KafkaConsumer(
        config.kafka.requests_topic,
        bootstrap_servers=config.kafka.bootstrap_servers,
        group_id=config.kafka.consumer_group,
        auto_offset_reset="earliest",
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        key_deserializer=lambda k: k.decode("utf-8") if k else None,
    )

    def shutdown(signum, frame):
        print("Worker shutting down...")
        consumer.close()
        sys.exit(0)

    signal.signal(signal.SIGTERM, shutdown)
    signal.signal(signal.SIGINT, shutdown)

    print(f"Worker listening on topic: {config.kafka.requests_topic}")

    for msg in consumer:
        event = msg.value
        job_id = event["jobId"]
        url = event["url"]
        format_str = event["format"]

        job = repository.find_by_id(job_id)
        if job is None:
            print(f"Job {job_id} not found in repository, skipping")
            continue

        try:
            job.status = JobStatus.PROCESSING
            fmt = DownloadFormat(format_str)
            file_path = downloader.download(job_id, url, fmt)
            job.file_name = file_path.name
            job.status = JobStatus.COMPLETED
            print(f"Job {job_id} completed: {job.file_name}")
        except Exception as e:
            job.error = str(e)
            job.status = JobStatus.FAILED
            print(f"Job {job_id} failed: {e}")


if __name__ == "__main__":
    main()
