package com.lsplit.service;

import com.lsplit.dto.request.CreateSettlementRequest;
import com.lsplit.dto.response.SettlementResponse;
import com.lsplit.entity.Group;
import com.lsplit.entity.Settlement;
import com.lsplit.entity.User;
import com.lsplit.exception.BadRequestException;
import com.lsplit.exception.ResourceNotFoundException;
import com.lsplit.exception.UnauthorizedException;
import com.lsplit.mapper.SettlementMapper;
import com.lsplit.model.SettlementStatus;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.GroupRepository;
import com.lsplit.repository.SettlementRepository;
import com.lsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SettlementMapper settlementMapper;

    public SettlementResponse createSettlement(UUID groupId, UUID payerId, CreateSettlementRequest req) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, payerId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, req.getPayeeId())) {
            throw new BadRequestException("Payee is not a member of this group");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));
        User payee = userRepository.findById(req.getPayeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found"));

        Settlement settlement = Settlement.builder()
                .group(group)
                .payer(payer)
                .payee(payee)
                .amount(req.getAmount())
                .status(SettlementStatus.COMPLETED)
                .build();
        settlementRepository.save(settlement);
        return settlementMapper.toSettlementResponse(settlement);
    }

    public List<SettlementResponse> getGroupSettlements(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        return settlementRepository.findByGroup_Id(groupId).stream()
                .map(settlementMapper::toSettlementResponse)
                .collect(Collectors.toList());
    }
}
