package com.splitledger.expense_service.repository;

import com.splitledger.expense_service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByGroupId(UUID groupId);
    List<Expense> findByGroupIdAndIsSettledFalse(UUID groupId);
}
