package com.lsplit.service;

import com.lsplit.dto.request.AddMemberRequest;
import com.lsplit.dto.request.CreateGroupRequest;
import com.lsplit.dto.response.GroupDetailResponse;
import com.lsplit.dto.response.GroupSummaryResponse;
import com.lsplit.dto.response.MemberResponse;
import com.lsplit.entity.Group;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.User;
import com.lsplit.exception.BadRequestException;
import com.lsplit.exception.ResourceNotFoundException;
import com.lsplit.exception.UnauthorizedException;
import com.lsplit.mapper.GroupMapper;
import com.lsplit.mapper.UserMapper;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.GroupRepository;
import com.lsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final BalanceService balanceService;

    public GroupDetailResponse createGroup(UUID creatorId, CreateGroupRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Group group = Group.builder()
                .name(req.getName())
                .description(req.getDescription())
                .createdBy(creator)
                .build();
        groupRepository.save(group);

        GroupMember.GroupMemberId memberId = new GroupMember.GroupMemberId(group.getId(), creatorId);
        GroupMember member = GroupMember.builder()
                .id(memberId)
                .group(group)
                .user(creator)
                .role("ADMIN")
                .build();
        groupMemberRepository.save(member);

        List<MemberResponse> members = List.of(userMapper.toMemberResponse(member));
        return groupMapper.toGroupDetailResponse(group, members);
    }

    public List<GroupSummaryResponse> getMyGroups(UUID userId) {
        List<GroupMember> memberships = groupMemberRepository.findByUser_Id(userId);
        return memberships.stream().map(m -> {
            Group group = m.getGroup();
            int memberCount = groupMemberRepository.findByGroup_Id(group.getId()).size();
            BigDecimal netBalance = balanceService.getUserNetBalance(group.getId(), userId);
            return groupMapper.toGroupSummaryResponse(group, memberCount, netBalance);
        }).collect(Collectors.toList());
    }

    public GroupDetailResponse getGroupDetail(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        List<MemberResponse> members = groupMemberRepository.findByGroup_Id(groupId).stream()
                .map(userMapper::toMemberResponse)
                .collect(Collectors.toList());
        return groupMapper.toGroupDetailResponse(group, members);
    }

    public MemberResponse addMember(UUID groupId, UUID requesterId, AddMemberRequest req) {
        GroupMember requesterMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        if (!"ADMIN".equals(requesterMember.getRole())) {
            throw new UnauthorizedException("Only admins can add members");
        }
        User newUser = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + req.getEmail()));
        if (groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, newUser.getId())) {
            throw new BadRequestException("User is already a member of this group");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        GroupMember.GroupMemberId id = new GroupMember.GroupMemberId(groupId, newUser.getId());
        GroupMember member = GroupMember.builder()
                .id(id)
                .group(group)
                .user(newUser)
                .role("MEMBER")
                .build();
        groupMemberRepository.save(member);
        return userMapper.toMemberResponse(member);
    }

    public void removeMember(UUID groupId, UUID requesterId, UUID targetUserId) {
        GroupMember requesterMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        if (!"ADMIN".equals(requesterMember.getRole())) {
            throw new UnauthorizedException("Only admins can remove members");
        }
        if (requesterId.equals(targetUserId)) {
            throw new BadRequestException("Admins cannot remove themselves");
        }
        GroupMember targetMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        if ("ADMIN".equals(targetMember.getRole())) {
            long adminCount = groupMemberRepository.findByGroup_Id(groupId).stream()
                    .filter(m -> "ADMIN".equals(m.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot remove the last admin");
            }
        }
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    public void deleteGroup(UUID groupId, UUID requesterId) {
        GroupMember requesterMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        if (!"ADMIN".equals(requesterMember.getRole())) {
            throw new UnauthorizedException("Only admins can delete the group");
        }
        groupRepository.deleteById(groupId);
    }
}
