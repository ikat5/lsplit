package com.lsplit.mapper;

import com.lsplit.dto.response.ExpenseItemResponse;
import com.lsplit.dto.response.ExpenseShareResponse;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.ExpenseShare;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExpenseMapper {

    private final UserMapper userMapper;

    public ExpenseShareResponse toExpenseShareResponse(ExpenseShare share) {
        return ExpenseShareResponse.builder()
                .userId(share.getUser().getId())
                .name(share.getUser().getName())
                .shareAmount(share.getShareAmount().setScale(2, RoundingMode.HALF_UP))
                .settled(share.isSettled())
                .build();
    }

    public ExpenseItemResponse toExpenseItemResponse(ExpenseItem expense) {
        List<ExpenseShare> shares = expense.getShares() != null
                ? expense.getShares()
                : Collections.emptyList();
        return ExpenseItemResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount().setScale(2, RoundingMode.HALF_UP))
                .splitType(expense.getSplitType().name())
                .paidBy(userMapper.toUserSummaryResponse(expense.getPaidBy()))
                .shares(shares.stream().map(this::toExpenseShareResponse).collect(Collectors.toList()))
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
