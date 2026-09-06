package com.splitledger.expense_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SplitRequest {
    private String userEmail;
    private BigDecimal amount;
    private Double percentage;
}
