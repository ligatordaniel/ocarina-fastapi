FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q test package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip nodejs ffmpeg ca-certificates \
    && python3 -m pip install --break-system-packages --no-cache-dir yt-dlp \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/ocarina-sprintboot-0.0.1-SNAPSHOT.jar /app/ocarina-sprintboot.jar
RUN mkdir -p /downloads
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/ocarina-sprintboot.jar"]
