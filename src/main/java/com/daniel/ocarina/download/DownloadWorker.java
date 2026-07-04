package com.daniel.ocarina.download;

import com.daniel.ocarina.config.OcarinaProperties;
import com.daniel.ocarina.kafka.DownloadRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class DownloadWorker {
    private final InMemoryDownloadJobRepository repository;
    private final YoutubeDownloadService youtubeDownloadService;
    private final OcarinaProperties properties;

    public DownloadWorker(InMemoryDownloadJobRepository repository,
                          YoutubeDownloadService youtubeDownloadService,
                          OcarinaProperties properties) {
        this.repository = repository;
        this.youtubeDownloadService = youtubeDownloadService;
        this.properties = properties;
    }

    @KafkaListener(topics = "#{@ocarinaProperties.kafka.requestsTopic}")
    public void consume(DownloadRequestedEvent event) {
        DownloadJob job = repository.findById(event.getJobId())
                .orElseGet(() -> repository.save(new DownloadJob(event.getJobId(), event.getUrl(), event.getFormat())));

        try {
            job.setStatus(JobStatus.PROCESSING);
            Path file = youtubeDownloadService.download(event.getJobId(), event.getUrl(), event.getFormat());
            job.setFileName(file.getFileName().toString());
            job.setStatus(JobStatus.COMPLETED);
        } catch (Exception ex) {
            job.setError(ex.getMessage());
            job.setStatus(JobStatus.FAILED);
        }
    }
}
