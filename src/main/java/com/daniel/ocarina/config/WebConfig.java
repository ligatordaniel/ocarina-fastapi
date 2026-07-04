package com.daniel.ocarina.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final OcarinaProperties properties;
    private final ShaulaAuthInterceptor shaulaAuthInterceptor;

    public WebConfig(OcarinaProperties properties, ShaulaAuthInterceptor shaulaAuthInterceptor) {
        this.properties = properties;
        this.shaulaAuthInterceptor = shaulaAuthInterceptor;
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(shaulaAuthInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(properties.getCors().getAllowedOrigins().split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
