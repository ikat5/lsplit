package com.lsplit.mapper;

import com.lsplit.dto.response.EventDetailResponse;
import com.lsplit.dto.response.EventSummaryResponse;
import com.lsplit.dto.response.ExpenseItemResponse;
import com.lsplit.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;

    public EventSummaryResponse toEventSummaryResponse(Event event, int expenseCount, BigDecimal totalAmount) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .expenseCount(expenseCount)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .createdAt(event.getCreatedAt())
                .build();
    }

    public EventDetailResponse toEventDetailResponse(Event event, List<ExpenseItemResponse> expenses,
                                                      int expenseCount, BigDecimal totalAmount) {
        return EventDetailResponse.builder()
                .id(event.getId())
                .groupId(event.getGroup().getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .createdBy(userMapper.toUserSummaryResponse(event.getCreatedBy()))
                .expenses(expenses)
                .expenseCount(expenseCount)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .createdAt(event.getCreatedAt())
                .build();
    }
}
