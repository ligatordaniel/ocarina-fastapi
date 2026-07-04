package com.daniel.ocarina.download;

import com.daniel.ocarina.config.OcarinaProperties;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/downloads")
public class DownloadController {
    private final DownloadService downloadService;
    private final Path downloadsDir;

    public DownloadController(DownloadService downloadService, OcarinaProperties properties) {
        this.downloadService = downloadService;
        this.downloadsDir = Path.of(properties.getDownloadsDir()).toAbsolutePath().normalize();
    }

    @PostMapping
    public ResponseEntity<DownloadJob> create(@Valid @RequestBody DownloadRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(downloadService.enqueue(request));
    }

    @GetMapping
    public List<DownloadJob> list() {
        return downloadService.list();
    }

    @GetMapping("/{id}")
    public DownloadJob get(@PathVariable String id) {
        return downloadService.get(id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<InputStreamResource> file(@PathVariable String id) throws IOException {
        DownloadJob job = downloadService.get(id);
        if (job.getStatus() != JobStatus.COMPLETED || job.getFileName() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Path file = downloadsDir.resolve(job.getFileName()).normalize();
        if (!file.startsWith(downloadsDir) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = job.getFormat() == DownloadFormat.MP3
                ? MediaType.parseMediaType("audio/mpeg")
                : MediaType.parseMediaType("video/mp4");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.getFileName() + "\"")
                .body(new InputStreamResource(Files.newInputStream(file)));
    }

    @ExceptionHandler(DownloadJobNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(DownloadJobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
