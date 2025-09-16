package com.paklog.cartonization.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${integration.rest.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${integration.rest.read-timeout:30000}")
    private int readTimeout;

    @Value("${integration.rest.max-connections:20}")
    private int maxConnections;

    @Value("${integration.rest.logging.enabled:true}")
    private boolean loggingEnabled;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(connectTimeout))
            .setReadTimeout(Duration.ofMillis(readTimeout))
            .requestFactory(() -> {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(connectTimeout);
                factory.setReadTimeout(readTimeout);
                return new BufferingClientHttpRequestFactory(factory);
            })
            .build();

        if (loggingEnabled) {
            restTemplate.getInterceptors().add(new LoggingInterceptor());
        }

        log.info("RestTemplate configured with connect timeout: {}ms, read timeout: {}ms", 
                connectTimeout, readTimeout);

        return restTemplate;
    }

    @Bean
    public RestTemplate productCatalogRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(3000))
            .setReadTimeout(Duration.ofMillis(15000))
            .requestFactory(() -> {
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(3000);
                factory.setReadTimeout(15000);
                return new BufferingClientHttpRequestFactory(factory);
            })
            .build();
    }

    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            long startTime = System.currentTimeMillis();
            
            if (log.isDebugEnabled()) {
                log.debug("HTTP {} {} - Request Body: {}", 
                        request.getMethod(), request.getURI(), new String(body, StandardCharsets.UTF_8));
            }

            ClientHttpResponse response = execution.execute(request, body);
            
            long duration = System.currentTimeMillis() - startTime;

            if (log.isDebugEnabled()) {
                String responseBody = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
                    
                log.debug("HTTP {} {} - {} {} ({}ms) - Response: {}", 
                        request.getMethod(), request.getURI(), 
                        response.getStatusCode().value(), response.getStatusText(), 
                        duration, responseBody);
            } else if (log.isInfoEnabled()) {
                log.info("HTTP {} {} - {} {} ({}ms)", 
                        request.getMethod(), request.getURI(), 
                        response.getStatusCode().value(), response.getStatusText(), duration);
            }

            return response;
        }
    }
}