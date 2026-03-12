package com.nexbank.auth.service;

import com.nexbank.auth.dto.AuthResponse;
import com.nexbank.auth.dto.LoginRequest;
import com.nexbank.auth.dto.RegisterRequest;
import com.nexbank.auth.dto.UserInfoResponse;
import com.nexbank.auth.entity.Role;
import com.nexbank.auth.entity.RoleName;
import com.nexbank.auth.entity.User;
import com.nexbank.auth.repository.RoleRepository;
import com.nexbank.auth.repository.UserRepository;
import com.nexbank.auth.security.JwtTokenProvider;
import com.nexbank.auth.security.UserDetailsImpl;
import com.nexbank.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación y gestión de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    /**
     * Registra un nuevo usuario en el sistema.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registrando nuevo usuario: {}", request.getEmail());

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "El email ya está registrado");
        }

        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "El username ya está en uso");
        }

        // Crear el usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Asignar rol USER por defecto
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER.name())
                .orElseGet(() -> {
                    // Si no existe el rol, crearlo
                    Role newRole = Role.builder()
                            .name(RoleName.ROLE_USER.name())
                            .description("Usuario Regular")
                            .build();
                    return roleRepository.save(newRole);
                });

        user.addRole(userRole);

        // Guardar usuario
        User savedUser = userRepository.save(user);

        // Generar tokens
        String accessToken = generateTokenForUser(savedUser);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getEmail());

        logger.info("Usuario registrado exitosamente: {}", savedUser.getEmail());

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    /**
     * Autentica un usuario y retorna tokens JWT.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        logger.info("Intento de login: {}", request.getEmail());

        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar tokens
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(request.getEmail());

        // Obtener usuario
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario no encontrado"));

        logger.info("Login exitoso: {}", request.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    /**
     * Obtiene información del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("NOT_AUTHENTICATED", "Usuario no autenticado");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Usuario no encontrado"));

        return buildUserInfoResponse(user);
    }

    /**
     * Genera un token JWT para un usuario
     */
    private String generateTokenForUser(User user) {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        return tokenProvider.generateToken(auth);
    }

    /**
     * Construye la respuesta de autenticación
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    /**
     * Construye la respuesta con información del usuario
     */
    private UserInfoResponse buildUserInfoResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}