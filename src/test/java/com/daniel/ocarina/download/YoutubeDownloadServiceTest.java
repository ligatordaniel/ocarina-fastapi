package com.daniel.ocarina.download;

import com.daniel.ocarina.config.OcarinaProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class YoutubeDownloadServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void rejectsNonYoutubeUrl() throws Exception {
        OcarinaProperties properties = new OcarinaProperties();
        properties.setDownloadsDir(tempDir.toString());
        YoutubeDownloadService service = new YoutubeDownloadService(properties);

        assertThrows(IllegalArgumentException.class, () -> service.download("job", "https://example.com/video", DownloadFormat.MP4));
    }
}
