package com.lsplit.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseShareResponse {

    private UUID userId;
    private String name;
    private BigDecimal shareAmount;

    @JsonProperty("isSettled")
    private boolean settled;
}
