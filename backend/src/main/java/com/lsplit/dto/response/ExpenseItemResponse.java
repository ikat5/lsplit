package com.lsplit.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseItemResponse {

    private UUID id;
    private String description;
    private BigDecimal amount;
    /** Serialized as the enum name string (e.g. "EQUAL", "EXACT", "PERCENTAGE"). */
    private String splitType;
    private UserSummaryResponse paidBy;
    private List<ExpenseShareResponse> shares;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
}
