package com.nexbank.account.entity;

/**
 * Estados de una cuenta bancaria.
 */
public enum AccountStatus {
    ACTIVE("Activa"),
    INACTIVE("Inactiva"),
    BLOCKED("Bloqueada"),
    CLOSED("Cerrada");
    
    private final String description;
    
    AccountStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
