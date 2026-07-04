package com.daniel.ocarina.download;

import com.daniel.ocarina.config.OcarinaProperties;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class YoutubeDownloadService {
    private static final Duration PROCESS_TIMEOUT = Duration.ofMinutes(20);
    private final Path downloadsDir;
    private final Path cookiesFile;

    public YoutubeDownloadService(OcarinaProperties properties) throws IOException {
        this.downloadsDir = Path.of(properties.getDownloadsDir()).toAbsolutePath().normalize();
        this.cookiesFile = properties.getYtDlpCookiesFile() == null || properties.getYtDlpCookiesFile().isBlank()
                ? null
                : Path.of(properties.getYtDlpCookiesFile()).toAbsolutePath().normalize();
        Files.createDirectories(this.downloadsDir);
    }

    public Path download(String jobId, String url, DownloadFormat format) throws IOException, InterruptedException {
        validateYoutubeUrl(url);

        List<String> command = new ArrayList<>();
        command.add("yt-dlp");
        command.add("--no-playlist");
        command.add("--restrict-filenames");
        command.add("--newline");
        command.add("--js-runtimes");
        command.add("node");
        if (cookiesFile != null && Files.exists(cookiesFile)) {
            command.add("--cookies");
            command.add(cookiesFile.toString());
        }
        command.add("-P");
        command.add(downloadsDir.toString());
        command.add("-o");
        command.add(jobId + ".%(ext)s");

        if (format == DownloadFormat.MP3) {
            command.add("--extract-audio");
            command.add("--audio-format");
            command.add("mp3");
            command.add("--audio-quality");
            command.add("0");
        } else {
            command.add("-f");
            command.add("bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
            command.add("--merge-output-format");
            command.add("mp4");
        }

        command.add(url);

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        boolean finished = process.waitFor(PROCESS_TIMEOUT.toSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("yt-dlp excedió el timeout de " + PROCESS_TIMEOUT.toMinutes() + " minutos");
        }
        if (process.exitValue() != 0) {
            throw new IOException("yt-dlp falló: " + trimOutput(output.toString()));
        }

        return locateOutputFile(jobId, format);
    }

    private Path locateOutputFile(String jobId, DownloadFormat format) throws IOException {
        String preferredExt = format == DownloadFormat.MP3 ? ".mp3" : ".mp4";
        Path fallback = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadsDir, jobId + ".*")) {
            for (Path candidate : stream) {
                if (Files.isRegularFile(candidate)) {
                    if (candidate.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(preferredExt)) {
                        return candidate;
                    }
                    fallback = candidate;
                }
            }
        }
        if (fallback != null) {
            return fallback;
        }
        throw new IOException("No se encontró el archivo descargado para job " + jobId);
    }

    private void validateYoutubeUrl(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            boolean validScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            boolean validHost = host.equals("youtu.be") || host.equals("youtube.com") || host.endsWith(".youtube.com");
            if (!validScheme || !validHost) {
                throw new IllegalArgumentException("URL no permitida. Usa youtube.com o youtu.be");
            }
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("URL de YouTube inválida", ex);
        }
    }

    private String trimOutput(String value) {
        if (value == null || value.isBlank()) {
            return "sin salida";
        }
        return value.length() > 1200 ? value.substring(value.length() - 1200) : value;
    }
}
