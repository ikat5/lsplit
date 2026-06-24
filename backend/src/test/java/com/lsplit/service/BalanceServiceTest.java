package com.lsplit.service;

import com.lsplit.dto.response.GroupBalanceResponse;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.ExpenseShare;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.Settlement;
import com.lsplit.entity.User;
import com.lsplit.repository.ExpenseItemRepository;
import com.lsplit.repository.ExpenseShareRepository;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.SettlementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BalanceServiceTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private ExpenseItemRepository expenseItemRepository;

    @Mock
    private ExpenseShareRepository expenseShareRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private BalanceService balanceService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private GroupMember mockMember(UUID groupId, UUID userId, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(name);

        GroupMember.GroupMemberId memberId = new GroupMember.GroupMemberId(groupId, userId);

        GroupMember member = mock(GroupMember.class);
        when(member.getId()).thenReturn(memberId);
        when(member.getUser()).thenReturn(user);
        return member;
    }

    private ExpenseItem mockExpense(User paidBy, BigDecimal amount) {
        ExpenseItem expense = mock(ExpenseItem.class);
        when(expense.getPaidBy()).thenReturn(paidBy);
        when(expense.getAmount()).thenReturn(amount);
        return expense;
    }

    private ExpenseShare mockShare(User user, BigDecimal shareAmount) {
        ExpenseShare share = mock(ExpenseShare.class);
        when(share.getUser()).thenReturn(user);
        when(share.getShareAmount()).thenReturn(shareAmount);
        return share;
    }

    // ---------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------

    @Test
    void testGetGroupBalances_noExpenses_zeroBalances() {
        UUID groupId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();

        GroupMember aliceMember = mockMember(groupId, aliceId, "Alice");
        GroupMember bobMember = mockMember(groupId, bobId, "Bob");

        when(groupMemberRepository.findByGroup_Id(groupId)).thenReturn(List.of(aliceMember, bobMember));
        when(expenseItemRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());
        when(expenseShareRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());
        when(settlementRepository.findByGroup_Id(groupId)).thenReturn(Collections.emptyList());

        GroupBalanceResponse response = balanceService.getGroupBalances(groupId);

        assertThat(response.getUserBalances()).hasSize(2);
        assertThat(response.getSuggestedPayments()).isEmpty();
        response.getUserBalances().forEach(ub ->
                assertThat(ub.getNetBalance().compareTo(BigDecimal.ZERO)).isEqualTo(0));
    }

    @Test
    void testGetGroupBalances_oneExpense_correctBalance() {
        UUID groupId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID carolId = UUID.randomUUID();

        // Real User mocks — reused across expense and shares
        User alice = mock(User.class);
        when(alice.getId()).thenReturn(aliceId);
        when(alice.getName()).thenReturn("Alice");

        User bob = mock(User.class);
        when(bob.getId()).thenReturn(bobId);
        when(bob.getName()).thenReturn("Bob");

        User carol = mock(User.class);
        when(carol.getId()).thenReturn(carolId);
        when(carol.getName()).thenReturn("Carol");

        // Members
        GroupMember aliceMember = mock(GroupMember.class);
        when(aliceMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, aliceId));
        when(aliceMember.getUser()).thenReturn(alice);

        GroupMember bobMember = mock(GroupMember.class);
        when(bobMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, bobId));
        when(bobMember.getUser()).thenReturn(bob);

        GroupMember carolMember = mock(GroupMember.class);
        when(carolMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, carolId));
        when(carolMember.getUser()).thenReturn(carol);

        // Alice paid $90
        ExpenseItem expense = mockExpense(alice, new BigDecimal("90"));

        // Shares: Alice $30, Bob $30, Carol $30
        ExpenseShare aliceShare = mockShare(alice, new BigDecimal("30"));
        ExpenseShare bobShare = mockShare(bob, new BigDecimal("30"));
        ExpenseShare carolShare = mockShare(carol, new BigDecimal("30"));

        when(groupMemberRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(aliceMember, bobMember, carolMember));
        when(expenseItemRepository.findByGroupId(groupId)).thenReturn(List.of(expense));
        when(expenseShareRepository.findByGroupId(groupId))
                .thenReturn(List.of(aliceShare, bobShare, carolShare));
        when(settlementRepository.findByGroup_Id(groupId)).thenReturn(Collections.emptyList());

        GroupBalanceResponse response = balanceService.getGroupBalances(groupId);

        // Net: Alice=+60, Bob=-30, Carol=-30
        assertThat(response.getUserBalances()).hasSize(3);
        GroupBalanceResponse.UserBalanceEntry aliceBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(aliceId)).findFirst().orElseThrow();
        GroupBalanceResponse.UserBalanceEntry bobBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(bobId)).findFirst().orElseThrow();
        GroupBalanceResponse.UserBalanceEntry carolBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(carolId)).findFirst().orElseThrow();

        assertThat(aliceBal.getNetBalance()).isEqualByComparingTo("60.00");
        assertThat(bobBal.getNetBalance()).isEqualByComparingTo("-30.00");
        assertThat(carolBal.getNetBalance()).isEqualByComparingTo("-30.00");

        // Suggested: Bob→Alice $30, Carol→Alice $30
        assertThat(response.getSuggestedPayments()).hasSize(2);
        response.getSuggestedPayments().forEach(p ->
                assertThat(p.getToUserId()).isEqualTo(aliceId));
        response.getSuggestedPayments().forEach(p ->
                assertThat(p.getAmount()).isEqualByComparingTo("30.00"));
    }

    @Test
    void testDebtSimplification_chainedDebts() {
        // Alice net=+80, Bob=-50, Carol=-30 (via one expense)
        // Expense: Alice paid $110, shares: Alice=$30, Bob=$50, Carol=$30
        UUID groupId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID carolId = UUID.randomUUID();

        User alice = mock(User.class);
        when(alice.getId()).thenReturn(aliceId);
        when(alice.getName()).thenReturn("Alice");

        User bob = mock(User.class);
        when(bob.getId()).thenReturn(bobId);
        when(bob.getName()).thenReturn("Bob");

        User carol = mock(User.class);
        when(carol.getId()).thenReturn(carolId);
        when(carol.getName()).thenReturn("Carol");

        GroupMember aliceMember = mock(GroupMember.class);
        when(aliceMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, aliceId));
        when(aliceMember.getUser()).thenReturn(alice);

        GroupMember bobMember = mock(GroupMember.class);
        when(bobMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, bobId));
        when(bobMember.getUser()).thenReturn(bob);

        GroupMember carolMember = mock(GroupMember.class);
        when(carolMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, carolId));
        when(carolMember.getUser()).thenReturn(carol);

        // Alice paid $110
        ExpenseItem expense = mockExpense(alice, new BigDecimal("110"));

        // Shares: Alice $30, Bob $50, Carol $30
        ExpenseShare aliceShare = mockShare(alice, new BigDecimal("30"));
        ExpenseShare bobShare = mockShare(bob, new BigDecimal("50"));
        ExpenseShare carolShare = mockShare(carol, new BigDecimal("30"));

        when(groupMemberRepository.findByGroup_Id(groupId))
                .thenReturn(List.of(aliceMember, bobMember, carolMember));
        when(expenseItemRepository.findByGroupId(groupId)).thenReturn(List.of(expense));
        when(expenseShareRepository.findByGroupId(groupId))
                .thenReturn(List.of(aliceShare, bobShare, carolShare));
        when(settlementRepository.findByGroup_Id(groupId)).thenReturn(Collections.emptyList());

        GroupBalanceResponse response = balanceService.getGroupBalances(groupId);

        // Net: Alice=+80, Bob=-50, Carol=-30
        GroupBalanceResponse.UserBalanceEntry aliceBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(aliceId)).findFirst().orElseThrow();
        GroupBalanceResponse.UserBalanceEntry bobBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(bobId)).findFirst().orElseThrow();
        GroupBalanceResponse.UserBalanceEntry carolBal = response.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(carolId)).findFirst().orElseThrow();

        assertThat(aliceBal.getNetBalance()).isEqualByComparingTo("80.00");
        assertThat(bobBal.getNetBalance()).isEqualByComparingTo("-50.00");
        assertThat(carolBal.getNetBalance()).isEqualByComparingTo("-30.00");

        // Suggested: Bob→Alice $50, Carol→Alice $30
        assertThat(response.getSuggestedPayments()).hasSize(2);

        GroupBalanceResponse.SuggestedPayment bobPayment = response.getSuggestedPayments().stream()
                .filter(p -> p.getFromUserId().equals(bobId)).findFirst().orElseThrow();
        assertThat(bobPayment.getToUserId()).isEqualTo(aliceId);
        assertThat(bobPayment.getAmount()).isEqualByComparingTo("50.00");

        GroupBalanceResponse.SuggestedPayment carolPayment = response.getSuggestedPayments().stream()
                .filter(p -> p.getFromUserId().equals(carolId)).findFirst().orElseThrow();
        assertThat(carolPayment.getToUserId()).isEqualTo(aliceId);
        assertThat(carolPayment.getAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void testGetUserNetBalance_returnsCorrectValue() {
        UUID groupId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();

        User alice = mock(User.class);
        when(alice.getId()).thenReturn(aliceId);
        when(alice.getName()).thenReturn("Alice");

        User bob = mock(User.class);
        when(bob.getId()).thenReturn(bobId);
        when(bob.getName()).thenReturn("Bob");

        GroupMember aliceMember = mock(GroupMember.class);
        when(aliceMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, aliceId));
        when(aliceMember.getUser()).thenReturn(alice);

        GroupMember bobMember = mock(GroupMember.class);
        when(bobMember.getId()).thenReturn(new GroupMember.GroupMemberId(groupId, bobId));
        when(bobMember.getUser()).thenReturn(bob);

        // Alice paid $60, shares: Alice=$30, Bob=$30
        ExpenseItem expense = mockExpense(alice, new BigDecimal("60"));
        ExpenseShare aliceShare = mockShare(alice, new BigDecimal("30"));
        ExpenseShare bobShare = mockShare(bob, new BigDecimal("30"));

        when(groupMemberRepository.findByGroup_Id(groupId)).thenReturn(List.of(aliceMember, bobMember));
        when(expenseItemRepository.findByGroupId(groupId)).thenReturn(List.of(expense));
        when(expenseShareRepository.findByGroupId(groupId)).thenReturn(List.of(aliceShare, bobShare));
        when(settlementRepository.findByGroup_Id(groupId)).thenReturn(Collections.emptyList());

        GroupBalanceResponse fullResponse = balanceService.getGroupBalances(groupId);
        BigDecimal aliceNetFromFull = fullResponse.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(aliceId))
                .map(GroupBalanceResponse.UserBalanceEntry::getNetBalance)
                .findFirst().orElseThrow();

        // getUserNetBalance calls getGroupBalances internally; repos are called again
        BigDecimal aliceNetDirect = balanceService.getUserNetBalance(groupId, aliceId);

        assertThat(aliceNetDirect).isEqualByComparingTo(aliceNetFromFull);
        // Alice paid 60, owes 30 → net = +30
        assertThat(aliceNetDirect).isEqualByComparingTo("30.00");
    }
}
