package com.lsplit.mapper;

import com.lsplit.dto.response.GroupDetailResponse;
import com.lsplit.dto.response.GroupSummaryResponse;
import com.lsplit.dto.response.MemberResponse;
import com.lsplit.entity.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupMapper {

    private final UserMapper userMapper;

    public GroupDetailResponse toGroupDetailResponse(Group group, List<MemberResponse> members) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(userMapper.toUserSummaryResponse(group.getCreatedBy()))
                .members(members)
                .createdAt(group.getCreatedAt())
                .build();
    }

    public GroupSummaryResponse toGroupSummaryResponse(Group group, int memberCount, BigDecimal myNetBalance) {
        return GroupSummaryResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .memberCount(memberCount)
                .myNetBalance(myNetBalance.setScale(2, RoundingMode.HALF_UP))
                .build();
    }
}
