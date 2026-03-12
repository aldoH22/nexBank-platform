package com.nexbank.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad User - Representa un usuario del sistema bancario.
 *
 * Características:
 * - Autenticación con email y password (encriptado con BCrypt)
 * - Roles múltiples (un usuario puede tener varios roles)
 * - Auditoría automática con createdAt y updatedAt
 * - Soft delete con campo enabled
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;  // Almacenado con BCrypt

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;  // Soft delete

    @Builder.Default
    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Relación Many-to-Many con Role
     * Un usuario puede tener múltiples roles
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Helper method para agregar un rol al usuario
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Helper method para verificar si el usuario tiene un rol específico
     */
    public boolean hasRole(String roleName) {
        return this.roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}
