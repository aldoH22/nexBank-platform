package com.nexbank.loan.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountResponse {
    private Long id;
    private Long userId;
    private String accountNumber;
    private String status;
    private BigDecimal balance;
    private String currency;
}
