import os
import subprocess
from pathlib import Path

from config import config
from models import DownloadFormat


class YoutubeDownloader:
    TIMEOUT_SECONDS = 20 * 60

    def __init__(self):
        self.downloads_dir = Path(config.downloads_dir).resolve()
        self.downloads_dir.mkdir(parents=True, exist_ok=True)
        self.cookies_file = None
        if config.yt_dlp_cookies_file:
            cf = Path(config.yt_dlp_cookies_file)
            if cf.exists():
                self.cookies_file = cf

    def download(self, job_id: str, url: str, format_: DownloadFormat) -> Path:
        self._validate_url(url)

        cmd = [
            "yt-dlp",
            "--no-playlist",
            "--restrict-filenames",
            "--newline",
            "--js-runtimes", "deno",
            "--remote-components", "ejs:github",
            "-P", str(self.downloads_dir),
            "-o", f"{job_id}.%(ext)s",
        ]

        if self.cookies_file:
            cmd += ["--cookies", str(self.cookies_file)]

        if format_ == DownloadFormat.MP3:
            cmd += [
                "--extract-audio",
                "--audio-format", "mp3",
                "--audio-quality", "0",
            ]
        else:
            cmd += [
                "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
                "--merge-output-format", "mp4",
            ]

        cmd.append(url)

        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=self.TIMEOUT_SECONDS,
        )

        if result.returncode != 0:
            output = result.stdout + result.stderr
            trimmed = output[-1200:] if len(output) > 1200 else (output or "sin salida")
            raise RuntimeError(f"yt-dlp falló: {trimmed}")

        return self._locate_file(job_id, format_)

    def _locate_file(self, job_id: str, format_: DownloadFormat) -> Path:
        preferred = ".mp3" if format_ == DownloadFormat.MP3 else ".mp4"
        fallback = None
        for f in self.downloads_dir.glob(f"{job_id}.*"):
            if f.is_file():
                if f.suffix.lower() == preferred:
                    return f
                fallback = f
        if fallback:
            return fallback
        raise FileNotFoundError(f"No se encontró archivo para job {job_id}")

    def _validate_url(self, url: str) -> None:
        from urllib.parse import urlparse
        parsed = urlparse(url.strip())
        if parsed.scheme not in ("http", "https"):
            raise ValueError("URL no permitida. Usa youtube.com o youtu.be")
        host = (parsed.hostname or "").lower()
        if host not in ("youtu.be", "youtube.com") and not host.endswith(".youtube.com"):
            raise ValueError("URL no permitida. Usa youtube.com o youtu.be")


downloader = YoutubeDownloader()
