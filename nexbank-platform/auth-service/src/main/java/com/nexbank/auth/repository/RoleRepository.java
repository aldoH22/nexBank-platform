package com.nexbank.auth.repository;

import com.nexbank.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Role.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Buscar rol por nombre
     */
    Optional<Role> findByName(String name);

    /**
     * Verificar si existe un rol con ese nombre
     */
    Boolean existsByName(String name);
}