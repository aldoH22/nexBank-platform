package com.nexbank.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Servidor de Eureka para Service Discovery.
 * 
 * Este servidor actúa como un registro centralizado donde todos
 * los microservicios se registran al iniciar. Permite que los
 * servicios se descubran entre sí dinámicamente sin necesidad
 * de conocer sus URLs/IPs hardcoded.
 * 
 * Dashboard disponible en: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}