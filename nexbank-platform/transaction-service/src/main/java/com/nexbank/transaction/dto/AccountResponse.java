package com.nexbank.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private Long userId;
    private String username;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    private String status;
    private BigDecimal dailyTransactionLimit;
    private String alias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
