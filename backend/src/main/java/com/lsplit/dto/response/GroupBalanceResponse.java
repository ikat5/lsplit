package com.lsplit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupBalanceResponse {

    private UUID groupId;
    private List<UserBalanceEntry> userBalances;
    private List<SuggestedPayment> suggestedPayments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserBalanceEntry {
        private UUID userId;
        private String name;
        private BigDecimal netBalance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuggestedPayment {
        private UUID fromUserId;
        private String fromName;
        private UUID toUserId;
        private String toName;
        private BigDecimal amount;
    }
}
