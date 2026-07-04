package com.daniel.ocarina.download;

import com.daniel.ocarina.config.OcarinaProperties;
import com.daniel.ocarina.kafka.DownloadRequestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DownloadService {
    private final InMemoryDownloadJobRepository repository;
    private final KafkaTemplate<String, DownloadRequestedEvent> kafkaTemplate;
    private final OcarinaProperties properties;

    public DownloadService(InMemoryDownloadJobRepository repository,
                           KafkaTemplate<String, DownloadRequestedEvent> kafkaTemplate,
                           OcarinaProperties properties) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public DownloadJob enqueue(DownloadRequest request) {
        DownloadJob job = new DownloadJob(UUID.randomUUID().toString(), request.getUrl().trim(), request.getFormat());
        repository.save(job);
        DownloadRequestedEvent event = new DownloadRequestedEvent(job.getId(), job.getUrl(), job.getFormat());
        kafkaTemplate.send(properties.getKafka().getRequestsTopic(), job.getId(), event);
        return job;
    }

    public List<DownloadJob> list() {
        return repository.findAll();
    }

    public DownloadJob get(String id) {
        return repository.findById(id).orElseThrow(() -> new DownloadJobNotFoundException(id));
    }
}
