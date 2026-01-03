package com.john.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {
	@Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
	
    @Bean
    WebClient clientWebClient(WebClient.Builder builder,
    							  @Value("${services.client.url}") String url) {
        return WebClient.builder()
            .baseUrl(url)
            .build();
    }
    
    @Bean
    WebClient orderWebClient(WebClient.Builder builder,
    							 @Value("${services.order.url}") String url) {
        return WebClient.builder()
            .baseUrl(url)
            .build();
    }
}