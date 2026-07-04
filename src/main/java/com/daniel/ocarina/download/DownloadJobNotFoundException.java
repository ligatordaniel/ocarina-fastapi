package com.daniel.ocarina.download;

public class DownloadJobNotFoundException extends RuntimeException {
    public DownloadJobNotFoundException(String id) {
        super("Job no encontrado: " + id);
    }
}
