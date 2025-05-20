package com.sidus.propert.model.jwt;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JwtRequest {
	@NotBlank
    private String username;
	@NotBlank
    private String password;
}