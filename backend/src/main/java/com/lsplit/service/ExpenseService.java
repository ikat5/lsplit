package com.lsplit.service;

import com.lsplit.dto.request.CreateExpenseRequest;
import com.lsplit.dto.request.ShareInput;
import com.lsplit.dto.response.ExpenseItemResponse;
import com.lsplit.entity.Event;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.ExpenseShare;
import com.lsplit.entity.User;
import com.lsplit.exception.BadRequestException;
import com.lsplit.exception.ResourceNotFoundException;
import com.lsplit.exception.UnauthorizedException;
import com.lsplit.mapper.ExpenseMapper;
import com.lsplit.repository.EventRepository;
import com.lsplit.repository.ExpenseItemRepository;
import com.lsplit.repository.ExpenseShareRepository;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseItemRepository expenseItemRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final EventRepository eventRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseItemResponse addExpense(UUID eventId, UUID creatorId, CreateExpenseRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        UUID groupId = event.getGroup().getId();

        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, req.getPaidByUserId())) {
            throw new BadRequestException("Paid-by user is not a member of this group");
        }
        User paidBy = userRepository.findById(req.getPaidByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Paid-by user not found"));

        ExpenseItem item = new ExpenseItem();
        item.setEvent(event);
        item.setDescription(req.getDescription());
        item.setAmount(req.getAmount());
        item.setSplitType(req.getSplitType());
        item.setPaidBy(paidBy);

        List<ExpenseShare> shares = calculateShares(req, item, groupId);
        item.setShares(shares);
        expenseItemRepository.save(item);
        return expenseMapper.toExpenseItemResponse(item);
    }

    private List<ExpenseShare> calculateShares(CreateExpenseRequest req, ExpenseItem item, UUID groupId) {
        BigDecimal total = req.getAmount();
        List<ExpenseShare> shares = new ArrayList<>();

        switch (req.getSplitType()) {
            case EQUAL -> {
                List<UUID> participants = req.getParticipantIds();
                if (participants == null || participants.isEmpty()) {
                    throw new BadRequestException("participantIds required for EQUAL split");
                }
                int count = participants.size();
                BigDecimal perShare = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                BigDecimal allocated = BigDecimal.ZERO;
                for (int i = 0; i < count; i++) {
                    User u = requireGroupMember(participants.get(i), groupId);
                    BigDecimal amt;
                    if (i == count - 1) {
                        amt = total.subtract(allocated);
                    } else {
                        amt = perShare;
                        allocated = allocated.add(perShare);
                    }
                    shares.add(buildShare(item, u, amt));
                }
            }
            case EXACT -> {
                if (req.getShares() == null || req.getShares().isEmpty()) {
                    throw new BadRequestException("shares required for EXACT split");
                }
                BigDecimal sum = req.getShares().stream()
                        .map(ShareInput::getValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (sum.compareTo(total) != 0) {
                    throw new BadRequestException("Share amounts must sum to the total amount");
                }
                for (ShareInput si : req.getShares()) {
                    User u = requireGroupMember(si.getUserId(), groupId);
                    shares.add(buildShare(item, u, si.getValue()));
                }
            }
            case PERCENTAGE -> {
                if (req.getShares() == null || req.getShares().isEmpty()) {
                    throw new BadRequestException("shares required for PERCENTAGE split");
                }
                BigDecimal pctSum = req.getShares().stream()
                        .map(ShareInput::getValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (pctSum.compareTo(BigDecimal.valueOf(100)) != 0) {
                    throw new BadRequestException("Percentages must sum to 100");
                }
                List<ShareInput> pctShares = req.getShares();
                BigDecimal pctAllocated = BigDecimal.ZERO;
                for (int i = 0; i < pctShares.size(); i++) {
                    ShareInput si = pctShares.get(i);
                    User u = requireGroupMember(si.getUserId(), groupId);
                    BigDecimal amt;
                    if (i == pctShares.size() - 1) {
                        amt = total.subtract(pctAllocated);
                    } else {
                        amt = total.multiply(si.getValue())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        pctAllocated = pctAllocated.add(amt);
                    }
                    shares.add(buildShare(item, u, amt));
                }
            }
            default -> throw new BadRequestException("Unknown split type: " + req.getSplitType());
        }
        return shares;
    }

    private User requireGroupMember(UUID userId, UUID groupId) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, userId)) {
            throw new BadRequestException("Participant is not a member of this group");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
    }

    private ExpenseShare buildShare(ExpenseItem item, User user, BigDecimal amount) {
        ExpenseShare share = new ExpenseShare();
        share.setExpenseItem(item);
        share.setUser(user);
        share.setShareAmount(amount);
        share.setSettled(false);
        return share;
    }

    public List<ExpenseItemResponse> getEventExpenses(UUID eventId, UUID requesterId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(event.getGroup().getId(), requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        return expenseItemRepository.findByEvent_Id(eventId).stream()
                .map(expenseMapper::toExpenseItemResponse)
                .collect(Collectors.toList());
    }

    public void deleteExpense(UUID expenseId, UUID requesterId) {
        ExpenseItem item = expenseItemRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        UUID groupId = item.getEvent().getGroup().getId();
        GroupMember requesterMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        boolean isCreator = item.getPaidBy().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterMember.getRole());
        if (!isCreator && !isAdmin) {
            throw new UnauthorizedException("Only the creator or an admin can delete this expense");
        }
        expenseItemRepository.deleteById(expenseId);
    }
}
