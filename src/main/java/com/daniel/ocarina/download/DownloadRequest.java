package com.daniel.ocarina.download;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DownloadRequest {
    @NotBlank
    private String url;

    @NotNull
    private DownloadFormat format;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public DownloadFormat getFormat() { return format; }
    public void setFormat(DownloadFormat format) { this.format = format; }
}
