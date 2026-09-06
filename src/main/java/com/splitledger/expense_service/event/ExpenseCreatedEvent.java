package com.splitledger.expense_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreatedEvent {

    private UUID eventId;
    private UUID groupId;
    private UUID expenseId;
    private UUID paidBy;
    private String paidByEmail;
    private BigDecimal totalAmount;
    private String description;
    private List<SplitDetail> splits;
    private String timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitDetail {
        private UUID userId;
        private String userEmail;
        private BigDecimal amount;
    }
}
