package com.nexbank.common.exception;

import lombok.Getter;

/**
 * Excepción base para errores de lógica de negocio.
 * Todos los servicios pueden lanzar esta excepción.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String code;
    
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}