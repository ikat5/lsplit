package com.lsplit.service;

import com.lsplit.dto.request.UpdateProfileRequest;
import com.lsplit.dto.response.UserSummaryResponse;
import com.lsplit.entity.User;
import com.lsplit.exception.ResourceNotFoundException;
import com.lsplit.mapper.UserMapper;
import com.lsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserSummaryResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserSummaryResponse(user);
    }

    public UserSummaryResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(req.getName());
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }
        userRepository.save(user);
        return userMapper.toUserSummaryResponse(user);
    }

    /**
     * Helper used by controllers to resolve the authenticated user from their email principal.
     */
    public User loadUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
