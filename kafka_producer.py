import json
from kafka import KafkaProducer

from config import config

_producer: KafkaProducer | None = None


def _get_producer() -> KafkaProducer:
    global _producer
    if _producer is None:
        _producer = KafkaProducer(
            bootstrap_servers=config.kafka.bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda k: k.encode("utf-8") if k else None,
        )
    return _producer


def send_download_request(job, shaula_cookie: str = "") -> None:
    event = {
        "jobId": job.id,
        "url": job.url,
        "format": job.format.value,
        "saveToKafra": job.save_to_kafra,
        "shaulaCookie": shaula_cookie,
    }
    producer = _get_producer()
    producer.send(
        config.kafka.requests_topic,
        key=job.id,
        value=event,
    )
    producer.flush()
