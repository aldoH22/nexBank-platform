package com.nexbank.auth.repository;

import com.nexbank.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Spring Data JPA genera automáticamente la implementación.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Buscar usuario por email (usado en login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Buscar usuario por username
     */
    Optional<User> findByUsername(String username);

    /**
     * Verificar si existe un usuario con ese email
     */
    Boolean existsByEmail(String email);

    /**
     * Verificar si existe un usuario con ese username
     */
    Boolean existsByUsername(String username);
}