package com.nexbank.auth.entity;

public enum RoleName {
    ROLE_USER("Usuario Regular"),
    ROLE_ADMIN("Administrador"),
    ROLE_OPERATOR("Operador Bancario"),
    ROLE_AUDITOR("Auditor");

    private final String description;

    RoleName(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
