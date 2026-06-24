package com.lsplit.dto.request;

import com.lsplit.model.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CreateExpenseRequest {

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private SplitType splitType;

    @NotNull
    private UUID paidByUserId;

    /** Required for EQUAL split; may be null for EXACT/PERCENTAGE. */
    private List<UUID> participantIds;

    /** Required for EXACT/PERCENTAGE split; may be null for EQUAL. */
    private List<ShareInput> shares;
}
