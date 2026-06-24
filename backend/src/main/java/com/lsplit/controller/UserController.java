package com.lsplit.controller;

import com.lsplit.dto.request.UpdateProfileRequest;
import com.lsplit.dto.response.UserSummaryResponse;
import com.lsplit.entity.User;
import com.lsplit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserSummaryResponse> updateMe(@RequestBody @Valid UpdateProfileRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.loadUserByEmail(auth.getName());
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), req));
    }
}
