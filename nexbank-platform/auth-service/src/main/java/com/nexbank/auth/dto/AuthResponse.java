package com.nexbank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para la respuesta de autenticación (login/register)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";  // Tipo de token
    private String refreshToken;
    private Long userId;
    private String username;
    private String email;
    private Set<String> roles;
}