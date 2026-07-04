package com.daniel.ocarina.download;

import java.time.Instant;

public class DownloadJob {
    private String id;
    private String url;
    private DownloadFormat format;
    private JobStatus status;
    private String fileName;
    private String error;
    private Instant createdAt;
    private Instant updatedAt;

    public DownloadJob() {}

    public DownloadJob(String id, String url, DownloadFormat format) {
        this.id = id;
        this.url = url;
        this.format = format;
        this.status = JobStatus.QUEUED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public DownloadFormat getFormat() { return format; }
    public void setFormat(DownloadFormat format) { this.format = format; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; this.updatedAt = Instant.now(); }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; this.updatedAt = Instant.now(); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
