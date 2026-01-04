package com.john.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
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
	@NotBlank(message = "Name is required")
	private String name;
	@NotBlank(message = "Surname is required")
    private String surname;
    private String patronymic;
    private String phoneNumber;
    @Email(message = "Invalid email format")
    private String email;
    private String pass;
    private Long role;
}
