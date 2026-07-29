package com.lsplit.mapper;

import com.lsplit.dto.response.MemberResponse;
import com.lsplit.dto.response.UserSummaryResponse;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryResponse toUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public MemberResponse toMemberResponse(GroupMember member) {
        return MemberResponse.builder()
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
