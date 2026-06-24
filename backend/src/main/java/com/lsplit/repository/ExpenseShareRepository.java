package com.lsplit.repository;

import com.lsplit.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, UUID> {

    List<ExpenseShare> findByExpenseItem_Id(UUID expenseItemId);

    @Query("SELECT es FROM ExpenseShare es WHERE es.expenseItem.event.group.id = :groupId")
    List<ExpenseShare> findByGroupId(@Param("groupId") UUID groupId);
}
