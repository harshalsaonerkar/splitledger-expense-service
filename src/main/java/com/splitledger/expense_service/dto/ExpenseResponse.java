package com.splitledger.expense_service.dto;

import com.splitledger.expense_service.enums.SplitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public class ExpenseResponse {

    private UUID id;
    private UUID groupId;
    private UUID paidBy;
    private String paidByEmail;
    private String description;
    private BigDecimal amount;
    private SplitType splitType;
    private boolean isSettled;
    private List<SplitDto> splits;

    @Data
    @Builder
    public static class SplitDto {
        private UUID userId;
        private String userEmail;
        private BigDecimal amount;
        private boolean isPaid;
    }
}
