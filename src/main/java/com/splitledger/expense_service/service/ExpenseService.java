package com.splitledger.expense_service.service;

import com.splitledger.expense_service.dto.CreateExpenseRequest;
import com.splitledger.expense_service.dto.ExpenseResponse;
import com.splitledger.expense_service.dto.SplitRequest;
import com.splitledger.expense_service.entity.Expense;
import com.splitledger.expense_service.entity.ExpenseSplit;
import com.splitledger.expense_service.enums.SplitType;
import com.splitledger.expense_service.event.ExpenseCreatedEvent;
import com.splitledger.expense_service.kafka.ExpenseEventPublisher;
import com.splitledger.expense_service.repository.ExpenseRepository;
import com.splitledger.expense_service.repository.ExpenseSplitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseEventPublisher eventPublisher;

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request,
                                         UUID payerId,
                                         String payerEmail) {
        // 1. Save expense
        Expense expense = Expense.builder()
                .groupId(request.getGroupId())
                .paidBy(payerId)
                .paidByEmail(payerEmail)
                .description(request.getDescription())
                .amount(request.getAmount())
                .splitType(request.getSplitType())
                .isSettled(false)
                .build();

        expenseRepository.save(expense);

        // 2. Compute and save splits
        List<ExpenseSplit> splits = computeSplits(expense, request);
        expenseSplitRepository.saveAll(splits);

        // 3. Publish Kafka event
        ExpenseCreatedEvent event = ExpenseCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .groupId(expense.getGroupId())
                .expenseId(expense.getId())
                .paidBy(payerId)
                .paidByEmail(payerEmail)
                .totalAmount(expense.getAmount())
                .description(expense.getDescription())
                .splits(splits.stream().map(s ->
                                ExpenseCreatedEvent.SplitDetail.builder()
                                        .userId(s.getUserId())
                                        .userEmail(s.getUserEmail())
                                        .amount(s.getAmount())
                                        .build())
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventPublisher.publishExpenseCreated(event);

        return toResponse(expense, splits);
    }

    public List<ExpenseResponse> getGroupExpenses(UUID groupId) {
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return expenses.stream().map(e -> {
            List<ExpenseSplit> splits = expenseSplitRepository
                    .findByExpenseId(e.getId());
            return toResponse(e, splits);
        }).collect(Collectors.toList());
    }

    public ExpenseResponse getExpense(UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        List<ExpenseSplit> splits = expenseSplitRepository
                .findByExpenseId(expenseId);
        return toResponse(expense, splits);
    }

    private List<ExpenseSplit> computeSplits(Expense expense,
                                             CreateExpenseRequest request) {
        List<ExpenseSplit> splits = new ArrayList<>();

        if (request.getSplitType() == SplitType.EQUAL) {
            int count = request.getSplits().size();
            BigDecimal share = expense.getAmount()
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

            for (SplitRequest s : request.getSplits()) {
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(UUID.randomUUID()) // resolved via group members
                        .userEmail(s.getUserEmail())
                        .amount(share)
                        .isPaid(false)
                        .build());
            }

        } else if (request.getSplitType() == SplitType.PERCENTAGE) {
            for (SplitRequest s : request.getSplits()) {
                BigDecimal amount = expense.getAmount()
                        .multiply(BigDecimal.valueOf(s.getPercentage() / 100))
                        .setScale(2, RoundingMode.HALF_UP);

                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(UUID.randomUUID())
                        .userEmail(s.getUserEmail())
                        .amount(amount)
                        .isPaid(false)
                        .build());
            }

        } else { // EXACT
            for (SplitRequest s : request.getSplits()) {
                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .userId(UUID.randomUUID())
                        .userEmail(s.getUserEmail())
                        .amount(s.getAmount())
                        .isPaid(false)
                        .build());
            }
        }

        return splits;
    }

    private ExpenseResponse toResponse(Expense expense,
                                       List<ExpenseSplit> splits) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroupId())
                .paidBy(expense.getPaidBy())
                .paidByEmail(expense.getPaidByEmail())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .splitType(expense.getSplitType())
                .isSettled(expense.isSettled())
                .splits(splits.stream().map(s ->
                                ExpenseResponse.SplitDto.builder()
                                        .userId(s.getUserId())
                                        .userEmail(s.getUserEmail())
                                        .amount(s.getAmount())
                                        .isPaid(s.isPaid())
                                        .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
