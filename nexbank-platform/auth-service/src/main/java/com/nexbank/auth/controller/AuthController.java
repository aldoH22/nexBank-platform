package com.nexbank.auth.controller;

import com.nexbank.auth.dto.*;
import com.nexbank.auth.service.AuthService;
import com.nexbank.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación.
 *
 * Endpoints:
 * - POST /api/auth/register - Registrar nuevo usuario
 * - POST /api/auth/login - Autenticar usuario
 * - GET /api/auth/me - Obtener info del usuario autenticado
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Endpoint para registrar un nuevo usuario.
     *
     * @param request - Datos del usuario a registrar
     * @return AuthResponse con tokens JWT
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        logger.info("POST /api/auth/register - Registrando usuario: {}", request.getEmail());

        AuthResponse authResponse = authService.register(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Usuario registrado exitosamente",
                authResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para autenticar un usuario existente.
     *
     * @param request - Credenciales (email y password)
     * @return AuthResponse con tokens JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        logger.info("POST /api/auth/login - Intento de login: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request);

        ApiResponse<AuthResponse> response = ApiResponse.success(
                "Login exitoso",
                authResponse
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener información del usuario autenticado.
     * Requiere JWT válido en el header Authorization.
     *
     * @return UserInfoResponse con datos del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        logger.info("GET /api/auth/me - Obteniendo usuario autenticado");

        UserInfoResponse userInfo = authService.getCurrentUser();

        ApiResponse<UserInfoResponse> response = ApiResponse.success(userInfo);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de prueba para verificar que el servicio está activo.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        ApiResponse<String> response = ApiResponse.success(
                "Auth Service is running",
                "OK"
        );
        return ResponseEntity.ok(response);
    }
}
