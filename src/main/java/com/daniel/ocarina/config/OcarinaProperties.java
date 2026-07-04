package com.daniel.ocarina.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ocarina")
public class OcarinaProperties {
    private String downloadsDir = "./downloads";
    private String ytDlpCookiesFile = "";
    private Kafka kafka = new Kafka();
    private Cors cors = new Cors();
    private Auth auth = new Auth();

    public String getDownloadsDir() { return downloadsDir; }
    public void setDownloadsDir(String downloadsDir) { this.downloadsDir = downloadsDir; }
    public String getYtDlpCookiesFile() { return ytDlpCookiesFile; }
    public void setYtDlpCookiesFile(String ytDlpCookiesFile) { this.ytDlpCookiesFile = ytDlpCookiesFile; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }
    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public static class Kafka {
        private String requestsTopic = "ocarina.download.requests";
        public String getRequestsTopic() { return requestsTopic; }
        public void setRequestsTopic(String requestsTopic) { this.requestsTopic = requestsTopic; }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:5173,http://localhost:8080";
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Auth {
        private boolean enabled = true;
        private String validateUrl = "http://shaula-sprintboot:8000/internal/validate";
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getValidateUrl() { return validateUrl; }
        public void setValidateUrl(String validateUrl) { this.validateUrl = validateUrl; }
    }
}
