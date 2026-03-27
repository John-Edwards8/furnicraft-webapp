package com.john.webapp.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
	@JsonAlias("orderId")
	private Long id;
	private Long clientId;
    private Date orderDate;
    private String status;
}
