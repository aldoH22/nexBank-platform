package com.nexbank.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nexbank.account.dto.UserResponse;

@FeignClient(
    name = "auth-service",        // Nombre registrado en Eureka
    path = "/api/auth"        // Path base del endpoint
)
public interface AuthServiceClient {

    /**
     * Consulta si un usuario existe y está activo en auth-service.
     * Retorna el usuario o lanza excepción si no existe.
     */
    @GetMapping("/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") Long userId);
}
