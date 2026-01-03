package com.john.webapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import com.john.webapp.dto.ClientResponseDto;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;

@Service
public class ClientServiceClient {
	private final WebClient cli;
	
	public ClientServiceClient(@Qualifier("clientWebClient") WebClient client) {
		this.cli = client;
	}
	
    public List<ClientResponseDto> getAllClients() {
    	return cli
                .get()
                .uri("/api/clients")
                .retrieve()
                .bodyToFlux(ClientResponseDto.class)
                .collectList()
                .onErrorResume(e ->{
                    return Mono.just(new ArrayList<>());
                })
                .blockOptional()
                .orElse(List.of());
	}
    
    public Optional<ClientResponseDto> getClientById(@PathVariable Long id) {
    	return Optional.ofNullable(cli
                .get()
                .uri("/api/clients/{id}", id)
                .retrieve()
                .bodyToMono(ClientResponseDto.class)
                .onErrorResume(e -> {
                    return Mono.just(new ClientResponseDto());
                })
                .block());
	}

	public ClientResponseDto createClient(@Valid ClientResponseDto client) {
    	return cli.post()
                .uri("/api/clients")
                .bodyValue(client)
                .retrieve()
                .bodyToMono(ClientResponseDto.class)
                .block();
	}
    
	public void updateClient(@PathVariable Long id, @Valid ClientResponseDto request) {
		cli
	        .put()
	        .uri("/api/clients/{id}", id)
	        .bodyValue(request)
	        .retrieve()
	        .toBodilessEntity()
	        .onErrorResume(e -> {
	            return Mono.error(new RuntimeException("Failed to update client: " + e.getMessage()));
	        })
	        .block();
	}
    
	public void deleteClient(@PathVariable Long id) {
		cli
	        .delete()
	        .uri("/api/clients/{id}", id)
	        .retrieve()
	        .toBodilessEntity()
	        .onErrorResume(e -> {
	            return Mono.error(new RuntimeException("Failed to delete client: " + e.getMessage()));
	        })
	        .block();
	}
	
}
