package com.daniel.ocarina.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class ShaulaAuthInterceptor implements HandlerInterceptor {
    private final OcarinaProperties properties;
    private final RestClient restClient;

    public ShaulaAuthInterceptor(OcarinaProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!properties.getAuth().isEnabled()) {
            return true;
        }

        if (isValidShaulaSession(request)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"No autorizado por Shaula\"}");
        return false;
    }

    private boolean isValidShaulaSession(HttpServletRequest request) {
        String validateUrl = properties.getAuth().getValidateUrl();
        if (validateUrl == null || validateUrl.isBlank()) {
            return false;
        }

        try {
            var result = restClient.get()
                    .uri(validateUrl)
                    .headers(headers -> forwardAuthHeaders(request, headers))
                    .retrieve()
                    .toBodilessEntity();
            return result.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            return false;
        }
    }

    private void forwardAuthHeaders(HttpServletRequest request, HttpHeaders headers) {
        String cookie = request.getHeader(HttpHeaders.COOKIE);
        if (cookie != null && !cookie.isBlank()) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            headers.add(HttpHeaders.AUTHORIZATION, authorization);
        }
    }
}
