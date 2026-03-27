package com.john.webapp.service;

import com.john.webapp.dto.CatalogItemDto;
import com.john.webapp.dto.EstimateLineItemDto;
import com.john.webapp.dto.EstimateResponseDto;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EstimateServiceClient {

    private final WebClient cli;

    public EstimateServiceClient(@Qualifier("estimateWebClient") WebClient client) {
        this.cli = client;
    }

    public List<EstimateResponseDto> getByOrderId(Long orderId) {
        return cli
                .get()
                .uri("/api/estimates/order/{orderId}", orderId)
                .retrieve()
                .bodyToFlux(EstimateResponseDto.class)
                .collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional()
                .orElse(List.of());
    }

    public Optional<EstimateResponseDto> getById(Long id) {
        return Optional.ofNullable(cli
                .get()
                .uri("/api/estimates/{id}", id)
                .retrieve()
                .bodyToMono(EstimateResponseDto.class)
                .onErrorResume(e -> Mono.empty())
                .block());
    }

    public EstimateResponseDto create(EstimateResponseDto dto) {
        return cli
                .post()
                .uri("/api/estimates")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(EstimateResponseDto.class)
                .block();
    }

    public EstimateResponseDto update(EstimateResponseDto dto) {
        return cli
                .put()
                .uri("/api/estimates/{id}", dto.getId())
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(EstimateResponseDto.class)
                .onErrorResume(e -> Mono.error(
                        new RuntimeException("Failed to update estimate: " + e.getMessage())))
                .block();
    }

    public EstimateResponseDto finalize(Long id) {
        return cli
                .patch()
                .uri("/api/estimates/{id}/finalize", id)
                .retrieve()
                .bodyToMono(EstimateResponseDto.class)
                .onErrorResume(e -> Mono.error(
                        new RuntimeException(e.getMessage())))
                .block();
    }

    public void delete(Long id) {
        cli
                .delete()
                .uri("/api/estimates/{id}", id)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> Mono.error(
                        new RuntimeException("Failed to delete estimate: " + e.getMessage())))
                .block();
    }
    
    public List<CatalogItemDto> getMaterials() {
        return cli.get().uri("/api/catalog/materials")
                .retrieve().bodyToFlux(CatalogItemDto.class).collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional().orElse(List.of());
    }
 
    public List<CatalogItemDto> getAccessories() {
        return cli.get().uri("/api/catalog/accessories")
                .retrieve().bodyToFlux(CatalogItemDto.class).collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional().orElse(List.of());
    }
 
    public List<CatalogItemDto> getProcesses() {
        return cli.get().uri("/api/catalog/processes")
                .retrieve().bodyToFlux(CatalogItemDto.class).collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional().orElse(List.of());
    }
    
    public List<EstimateLineItemDto> getItems(Long estimateId) {
        return cli.get().uri("/api/estimates/{id}/items", estimateId)
                .retrieve().bodyToFlux(EstimateLineItemDto.class).collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .blockOptional().orElse(List.of());
    }
 
    public void addItem(Long estimateId, Long itemId,
                        CatalogItemDto.Type type, Integer amount) {
        Map<String, Object> body = Map.of(
                "itemId", itemId,
                "type", type.name(),
                "amount", amount);
        cli.post().uri("/api/estimates/{id}/items", estimateId)
                .bodyValue(body).retrieve().toBodilessEntity()
                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())))
                .block();
    }
 
    public void removeItem(Long estimateId, Long itemId, CatalogItemDto.Type type) {
        cli.delete()
                .uri("/api/estimates/{id}/items/{itemId}?type={type}",
                        estimateId, itemId, type.name())
                .retrieve().toBodilessEntity()
                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())))
                .block();
    }
}