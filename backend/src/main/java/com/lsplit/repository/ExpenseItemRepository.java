package com.lsplit.repository;

import com.lsplit.entity.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {

    List<ExpenseItem> findByEvent_Id(UUID eventId);

    @Query("SELECT e FROM ExpenseItem e WHERE e.event.group.id = :groupId")
    List<ExpenseItem> findByGroupId(@Param("groupId") UUID groupId);
}
