package com.lsplit.controller;

import com.lsplit.dto.request.AddMemberRequest;
import com.lsplit.dto.request.CreateGroupRequest;
import com.lsplit.dto.request.CreateSettlementRequest;
import com.lsplit.dto.response.GroupBalanceResponse;
import com.lsplit.dto.response.GroupDetailResponse;
import com.lsplit.dto.response.GroupSummaryResponse;
import com.lsplit.dto.response.MemberResponse;
import com.lsplit.dto.response.SettlementResponse;
import com.lsplit.entity.User;
import com.lsplit.exception.UnauthorizedException;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.service.BalanceService;
import com.lsplit.service.GroupService;
import com.lsplit.service.SettlementService;
import com.lsplit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final BalanceService balanceService;
    private final SettlementService settlementService;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.loadUserByEmail(auth.getName());
    }

    @PostMapping
    public ResponseEntity<GroupDetailResponse> createGroup(@RequestBody @Valid CreateGroupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(currentUser().getId(), req));
    }

    @GetMapping
    public ResponseEntity<List<GroupSummaryResponse>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups(currentUser().getId()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(groupId, currentUser().getId()));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        groupService.deleteGroup(groupId, currentUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<MemberResponse> addMember(
            @PathVariable UUID groupId,
            @RequestBody @Valid AddMemberRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.addMember(groupId, currentUser().getId(), req));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        groupService.removeMember(groupId, currentUser().getId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<GroupBalanceResponse> getGroupBalances(@PathVariable UUID groupId) {
        User user = currentUser();
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, user.getId())) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        return ResponseEntity.ok(balanceService.getGroupBalances(groupId));
    }

    @GetMapping("/{groupId}/settlements")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(@PathVariable UUID groupId) {
        return ResponseEntity.ok(settlementService.getGroupSettlements(groupId, currentUser().getId()));
    }

    @PostMapping("/{groupId}/settlements")
    public ResponseEntity<SettlementResponse> createSettlement(
            @PathVariable UUID groupId,
            @RequestBody @Valid CreateSettlementRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settlementService.createSettlement(groupId, currentUser().getId(), req));
    }
}
