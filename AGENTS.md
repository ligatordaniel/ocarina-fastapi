# ocarina-sprintboot

Backend Spring Boot personal de Daniel.

Reglas rápidas:
- Código simple y legible.
- No imprimir URLs privadas ni secretos en logs.
- Descargas solo por `yt-dlp` usando `ProcessBuilder` con argumentos separados.
- Kafka es el bus entre request HTTP y worker.
- Verificar con `mvn test` o Docker build antes de decir listo.
