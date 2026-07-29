package com.lsplit.service;

import com.lsplit.dto.response.GroupBalanceResponse;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.ExpenseShare;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.Settlement;
import com.lsplit.repository.ExpenseItemRepository;
import com.lsplit.repository.ExpenseShareRepository;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceService {

    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final SettlementRepository settlementRepository;

    public GroupBalanceResponse getGroupBalances(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroup_Id(groupId);
        List<ExpenseItem> expenses = expenseItemRepository.findByGroupId(groupId);
        List<ExpenseShare> shares = expenseShareRepository.findByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroup_Id(groupId);

        Map<UUID, String> userNames = members.stream()
                .collect(Collectors.toMap(
                        m -> m.getId().getUserId(),
                        m -> m.getUser().getName()
                ));

        Map<UUID, BigDecimal> netBalances = new HashMap<>();
        for (GroupMember member : members) {
            UUID uid = member.getId().getUserId();

            BigDecimal totalPaid = expenses.stream()
                    .filter(e -> e.getPaidBy().getId().equals(uid))
                    .map(ExpenseItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOwed = shares.stream()
                    .filter(s -> s.getUser().getId().equals(uid))
                    .map(ExpenseShare::getShareAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal settlPaid = settlements.stream()
                    .filter(s -> s.getPayer().getId().equals(uid))
                    .map(Settlement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal settlReceived = settlements.stream()
                    .filter(s -> s.getPayee().getId().equals(uid))
                    .map(Settlement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal net = totalPaid.subtract(totalOwed).add(settlPaid).subtract(settlReceived);
            netBalances.put(uid, net);
        }

        List<GroupBalanceResponse.UserBalanceEntry> userBalances = netBalances.entrySet().stream()
                .map(e -> new GroupBalanceResponse.UserBalanceEntry(
                        e.getKey(),
                        userNames.get(e.getKey()),
                        e.getValue().setScale(2, RoundingMode.HALF_UP)
                ))
                .collect(Collectors.toList());

        List<GroupBalanceResponse.SuggestedPayment> payments = simplifyDebts(netBalances, userNames);

        return new GroupBalanceResponse(groupId, userBalances, payments);
    }

    private List<GroupBalanceResponse.SuggestedPayment> simplifyDebts(
            Map<UUID, BigDecimal> netBalances, Map<UUID, String> names) {

        List<UserBal> debtors = new ArrayList<>();
        List<UserBal> creditors = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> e : netBalances.entrySet()) {
            UserBal ub = new UserBal(e.getKey(), names.get(e.getKey()), e.getValue());
            if (e.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(ub);
            } else if (e.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(ub);
            }
        }

        // Sort debtors ascending (most negative first) and creditors descending (largest credit first)
        debtors.sort(Comparator.comparing(ub -> ub.balance));
        creditors.sort((a, b) -> b.balance.compareTo(a.balance));

        List<GroupBalanceResponse.SuggestedPayment> payments = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            UserBal debtor = debtors.get(i);
            UserBal creditor = creditors.get(j);
            BigDecimal amt = debtor.balance.abs().min(creditor.balance);

            payments.add(new GroupBalanceResponse.SuggestedPayment(
                    debtor.id, debtor.name,
                    creditor.id, creditor.name,
                    amt.setScale(2, RoundingMode.HALF_UP)
            ));

            debtor.balance = debtor.balance.add(amt);
            creditor.balance = creditor.balance.subtract(amt);

            if (debtor.balance.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.balance.compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return payments;
    }

    public BigDecimal getUserNetBalance(UUID groupId, UUID userId) {
        GroupBalanceResponse resp = getGroupBalances(groupId);
        return resp.getUserBalances().stream()
                .filter(ub -> ub.getUserId().equals(userId))
                .map(GroupBalanceResponse.UserBalanceEntry::getNetBalance)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    /** Mutable balance holder used in the debt-simplification algorithm. */
    private static class UserBal {
        UUID id;
        String name;
        BigDecimal balance;

        UserBal(UUID id, String name, BigDecimal balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }
    }
}
