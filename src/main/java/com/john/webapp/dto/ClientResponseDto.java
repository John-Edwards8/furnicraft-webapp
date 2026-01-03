package com.john.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {
	private Long id;
	private String name;
    private String surname;
    private String patronymic;
    private String phoneNumber;
    private String email;
    private String pass;
    private Long role;
}
