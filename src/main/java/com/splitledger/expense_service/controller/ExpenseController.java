package com.splitledger.expense_service.controller;


import com.splitledger.expense_service.dto.CreateExpenseRequest;
import com.splitledger.expense_service.dto.ExpenseResponse;
import com.splitledger.expense_service.security.JwtTokenProvider;
import com.splitledger.expense_service.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request, HttpServletRequest httpRequest) {

        String token = httpRequest.getHeader("Authorization").substring(7);
        UUID payerId = jwtTokenProvider.extractUserId(token);
        String payerEmail = jwtTokenProvider.extractEmail(token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request, payerId, payerEmail));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable UUID expenseId) {
        return ResponseEntity.ok(expenseService.getExpense(expenseId));
    }
}
