package com.nexbank.account.entity;

/**
 * Tipos de cuentas bancarias disponibles.
 */
public enum AccountType {
    SAVINGS("Cuenta de Ahorro"),
    CHECKING("Cuenta Corriente"),
    INVESTMENT("Cuenta de Inversión");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
