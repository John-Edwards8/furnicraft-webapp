package com.john.webapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import com.john.webapp.dto.OrderResponseDto;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Service
public class OrderServiceClient {
	private final WebClient cli;

    public OrderServiceClient(@Qualifier("orderWebClient") WebClient client) {
    	this.cli = client;
    }
    
    public List<OrderResponseDto> getAllOrders() {
        return cli.get()
            .uri("/api/orders")
            .retrieve()
            .bodyToFlux(OrderResponseDto.class)
            .collectList()
            .onErrorResume(e ->{
                return Mono.just(new ArrayList<>());
            })
            .blockOptional()
            .orElse(List.of());
    }

    public List<OrderResponseDto> getOrdersByClientId(Long clientId) {
        return cli.get()
            .uri("/api/orders/client/{id}", clientId)
            .retrieve()
            .bodyToFlux(OrderResponseDto.class)
            .collectList()
            .onErrorResume(e ->{
                return Mono.just(new ArrayList<>());
            })
            .blockOptional()
            .orElse(List.of());
    }
    
    public Optional<OrderResponseDto> getOrderById(@PathVariable Long id) {
        return Optional.ofNullable(cli
                .get()
                .uri("/api/orders/{id}", id)
                .retrieve()
                .bodyToMono(OrderResponseDto.class)
                .onErrorResume(e -> Mono.just(new OrderResponseDto()))
                .block());
    }
    
    public OrderResponseDto createOrder(@Valid OrderResponseDto order, Long clientId) {
    	return cli.post()
    			.uri("/api/orders/{clientId}", clientId)
    			.bodyValue(order)
    			.retrieve()
    			.bodyToMono(OrderResponseDto.class)
    			.block();
    }
    
    public void updateOrder(@Valid OrderResponseDto request) {
    	cli.put()
    	   .uri("/api/orders/{id}", request.getId())
    	   .bodyValue(request)
    	   .retrieve()
    	   .toBodilessEntity()
    	   .onErrorResume(e -> {
	            return Mono.error(new RuntimeException("Failed to update order: " + e.getMessage()));
	        })
    	   .block();
    }
    
    public void deleteOrder(Long orderId, Long clientId) {
    	cli.delete()
    	   .uri("/api/orders/{orderId}/{clientId}", orderId, clientId)
    	   .retrieve()
    	   .toBodilessEntity()
    	   .onErrorResume(e -> {
	            return Mono.error(new RuntimeException("Failed to delete order: " + e.getMessage()));
	        })
    	   .block();
		
	}
}
