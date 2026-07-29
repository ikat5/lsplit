package com.lsplit.controller;

import com.lsplit.dto.request.CreateExpenseRequest;
import com.lsplit.dto.response.ExpenseItemResponse;
import com.lsplit.entity.User;
import com.lsplit.service.ExpenseService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.loadUserByEmail(auth.getName());
    }

    @PostMapping("/events/{eventId}/expenses")
    public ResponseEntity<ExpenseItemResponse> addExpense(
            @PathVariable UUID eventId,
            @RequestBody @Valid CreateExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.addExpense(eventId, currentUser().getId(), req));
    }

    @GetMapping("/events/{eventId}/expenses")
    public ResponseEntity<List<ExpenseItemResponse>> getEventExpenses(@PathVariable UUID eventId) {
        return ResponseEntity.ok(expenseService.getEventExpenses(eventId, currentUser().getId()));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId, currentUser().getId());
        return ResponseEntity.noContent().build();
    }
}
