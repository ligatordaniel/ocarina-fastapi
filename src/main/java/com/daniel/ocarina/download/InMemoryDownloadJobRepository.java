package com.daniel.ocarina.download;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDownloadJobRepository {
    private final ConcurrentHashMap<String, DownloadJob> jobs = new ConcurrentHashMap<>();

    public DownloadJob save(DownloadJob job) {
        jobs.put(job.getId(), job);
        return job;
    }

    public Optional<DownloadJob> findById(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public List<DownloadJob> findAll() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(DownloadJob::getCreatedAt).reversed())
                .toList();
    }
}
