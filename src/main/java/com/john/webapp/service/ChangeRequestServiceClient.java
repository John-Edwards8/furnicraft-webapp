package com.john.webapp.service;

import com.john.webapp.dto.ChangeRequestDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChangeRequestServiceClient {

    private final WebClient cli;

    public ChangeRequestServiceClient(@Qualifier("estimateWebClient") WebClient client) {
        this.cli = client;
    }

    public List<ChangeRequestDto> getByEstimateId(Long estimateId) {
        return cli.get()
                .uri("/api/change-requests/estimate/{id}", estimateId)
                .retrieve()
                .bodyToFlux(ChangeRequestDto.class)
                .collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional().orElse(List.of());
    }

    public ChangeRequestDto create(ChangeRequestDto dto) {
        return cli.post()
                .uri("/api/change-requests")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(ChangeRequestDto.class)
                .block();
    }

    public void updateStatus(Long id, String status) {
        cli.patch()
                .uri("/api/change-requests/{id}/status", id)
                .bodyValue(Map.of("status", status))
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())))
                .block();
    }

    public long countNew() {
        try {
            Map<?, ?> result = cli.get()
                    .uri("/api/change-requests/count-new")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return result != null
                    ? ((Number) result.get("count")).longValue()
                    : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}