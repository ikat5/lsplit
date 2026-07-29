package com.lsplit.mapper;

import com.lsplit.dto.response.SettlementResponse;
import com.lsplit.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class SettlementMapper {

    private final UserMapper userMapper;

    public SettlementResponse toSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .groupId(settlement.getGroup().getId())
                .payer(userMapper.toUserSummaryResponse(settlement.getPayer()))
                .payee(userMapper.toUserSummaryResponse(settlement.getPayee()))
                .amount(settlement.getAmount().setScale(2, RoundingMode.HALF_UP))
                .status(settlement.getStatus().name())
                .settledAt(settlement.getSettledAt())
                .build();
    }
}
