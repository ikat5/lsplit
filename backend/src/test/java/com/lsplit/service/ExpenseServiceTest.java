package com.lsplit.service;

import com.lsplit.dto.request.CreateExpenseRequest;
import com.lsplit.dto.request.ShareInput;
import com.lsplit.entity.Event;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.Group;
import com.lsplit.entity.User;
import com.lsplit.exception.BadRequestException;
import com.lsplit.mapper.ExpenseMapper;
import com.lsplit.model.SplitType;
import com.lsplit.repository.EventRepository;
import com.lsplit.repository.ExpenseItemRepository;
import com.lsplit.repository.ExpenseShareRepository;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpenseServiceTest {

    @Mock
    private ExpenseItemRepository expenseItemRepository;

    @Mock
    private ExpenseShareRepository expenseShareRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseService expenseService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private UUID setupEventAndMembership(UUID groupId) {
        UUID eventId = UUID.randomUUID();

        Group group = mock(Group.class);
        when(group.getId()).thenReturn(groupId);

        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getGroup()).thenReturn(group);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        return eventId;
    }

    private User mockUser(UUID userId) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        return user;
    }

    // ---------------------------------------------------------------
    // EQUAL split tests
    // ---------------------------------------------------------------

    @Test
    void testAddExpense_equalSplit_correctShares() {
        UUID groupId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        UUID eventId = setupEventAndMembership(groupId);

        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)).thenReturn(true);
        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, user1Id)).thenReturn(true);

        mockUser(user1Id);
        mockUser(user2Id);
        mockUser(user3Id);

        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .description("Dinner")
                .amount(new BigDecimal("90"))
                .splitType(SplitType.EQUAL)
                .paidByUserId(user1Id)
                .participantIds(List.of(user1Id, user2Id, user3Id))
                .build();

        when(expenseMapper.toExpenseItemResponse(any(ExpenseItem.class))).thenReturn(null);

        expenseService.addExpense(eventId, creatorId, req);

        ArgumentCaptor<ExpenseItem> captor = ArgumentCaptor.forClass(ExpenseItem.class);
        org.mockito.Mockito.verify(expenseItemRepository).save(captor.capture());
        ExpenseItem saved = captor.getValue();

        assertThat(saved.getShares()).hasSize(3);
        saved.getShares().forEach(share ->
                assertThat(share.getShareAmount()).isEqualByComparingTo("30.00"));
    }

    @Test
    void testAddExpense_equalSplit_roundingAdjustment() {
        UUID groupId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        UUID user3Id = UUID.randomUUID();

        UUID eventId = setupEventAndMembership(groupId);

        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)).thenReturn(true);
        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, user1Id)).thenReturn(true);

        mockUser(user1Id);
        mockUser(user2Id);
        mockUser(user3Id);

        // $10 / 3 → $3.33, $3.33, $3.34
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .description("Coffee")
                .amount(new BigDecimal("10"))
                .splitType(SplitType.EQUAL)
                .paidByUserId(user1Id)
                .participantIds(List.of(user1Id, user2Id, user3Id))
                .build();

        when(expenseMapper.toExpenseItemResponse(any(ExpenseItem.class))).thenReturn(null);

        expenseService.addExpense(eventId, creatorId, req);

        ArgumentCaptor<ExpenseItem> captor = ArgumentCaptor.forClass(ExpenseItem.class);
        org.mockito.Mockito.verify(expenseItemRepository).save(captor.capture());
        ExpenseItem saved = captor.getValue();

        assertThat(saved.getShares()).hasSize(3);
        assertThat(saved.getShares().get(0).getShareAmount()).isEqualByComparingTo("3.33");
        assertThat(saved.getShares().get(1).getShareAmount()).isEqualByComparingTo("3.33");
        assertThat(saved.getShares().get(2).getShareAmount()).isEqualByComparingTo("3.34");

        BigDecimal total = saved.getShares().stream()
                .map(s -> s.getShareAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("10.00");
    }

    // ---------------------------------------------------------------
    // EXACT split tests
    // ---------------------------------------------------------------

    @Test
    void testAddExpense_exactSplit_sumMismatch_throwsBadRequest() {
        UUID groupId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        UUID eventId = setupEventAndMembership(groupId);

        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)).thenReturn(true);
        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, payerId)).thenReturn(true);
        mockUser(payerId);

        // Shares sum to 80, but total is 90
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .description("Groceries")
                .amount(new BigDecimal("90"))
                .splitType(SplitType.EXACT)
                .paidByUserId(payerId)
                .shares(List.of(
                        new ShareInput(user1Id, new BigDecimal("50")),
                        new ShareInput(user2Id, new BigDecimal("30"))
                ))
                .build();

        assertThatThrownBy(() -> expenseService.addExpense(eventId, creatorId, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Share amounts must sum to the total amount");
    }

    // ---------------------------------------------------------------
    // PERCENTAGE split tests
    // ---------------------------------------------------------------

    @Test
    void testAddExpense_percentageSplit_sumNot100_throwsBadRequest() {
        UUID groupId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        UUID eventId = setupEventAndMembership(groupId);

        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)).thenReturn(true);
        when(groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, payerId)).thenReturn(true);
        mockUser(payerId);

        // Percentages sum to 99, not 100
        CreateExpenseRequest req = CreateExpenseRequest.builder()
                .description("Trip costs")
                .amount(new BigDecimal("200"))
                .splitType(SplitType.PERCENTAGE)
                .paidByUserId(payerId)
                .shares(List.of(
                        new ShareInput(user1Id, new BigDecimal("60")),
                        new ShareInput(user2Id, new BigDecimal("39"))
                ))
                .build();

        assertThatThrownBy(() -> expenseService.addExpense(eventId, creatorId, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Percentages must sum to 100");
    }
}
