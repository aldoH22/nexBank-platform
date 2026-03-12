package com.nexbank.auth.config;

import com.nexbank.auth.entity.Role;
import com.nexbank.auth.entity.RoleName;
import com.nexbank.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa datos necesarios en la BD al arrancar la aplicación.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initializeRoles();
    }

    private void initializeRoles() {
        logger.info("Inicializando roles...");

        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName.name())) {
                Role role = Role.builder()
                        .name(roleName.name())
                        .description(roleName.getDescription())
                        .build();
                roleRepository.save(role);
                logger.info("Rol creado: {}", roleName.name());
            }
        }

        logger.info("Roles inicializados correctamente");
    }
}
