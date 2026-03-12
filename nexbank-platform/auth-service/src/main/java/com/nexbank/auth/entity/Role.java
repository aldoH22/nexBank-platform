package com.nexbank.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Role - Representa un rol del sistema.
 *
 * Roles del sistema bancario:
 * - ROLE_USER: Cliente bancario regular
 * - ROLE_ADMIN: Administrador del sistema
 * - ROLE_OPERATOR: Operador bancario (empleado)
 * - ROLE_AUDITOR: Auditor (solo lectura)
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;  // Ej: ROLE_USER, ROLE_ADMIN

    @Column(length = 100)
    private String description;

    // Constructor de conveniencia
    public Role(String name) {
        this.name = name;
    }
}
