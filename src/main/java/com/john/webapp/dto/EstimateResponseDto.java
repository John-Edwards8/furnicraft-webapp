package com.john.webapp.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateResponseDto {
	private Long id;
	@NotBlank(message = "Name is required")
    private String name;
    private String description;
    private LocalDate date;
    private Boolean isFinal;
    private Long orderId;
}
