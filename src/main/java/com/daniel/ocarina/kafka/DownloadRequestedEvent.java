package com.daniel.ocarina.kafka;

import com.daniel.ocarina.download.DownloadFormat;

public class DownloadRequestedEvent {
    private String jobId;
    private String url;
    private DownloadFormat format;

    public DownloadRequestedEvent() {}

    public DownloadRequestedEvent(String jobId, String url, DownloadFormat format) {
        this.jobId = jobId;
        this.url = url;
        this.format = format;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public DownloadFormat getFormat() { return format; }
    public void setFormat(DownloadFormat format) { this.format = format; }
}
