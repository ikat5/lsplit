package com.lsplit.controller;

import com.lsplit.dto.request.CreateEventRequest;
import com.lsplit.dto.response.EventDetailResponse;
import com.lsplit.dto.response.EventSummaryResponse;
import com.lsplit.entity.User;
import com.lsplit.service.EventService;
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
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.loadUserByEmail(auth.getName());
    }

    @PostMapping("/groups/{groupId}/events")
    public ResponseEntity<EventSummaryResponse> createEvent(
            @PathVariable UUID groupId,
            @RequestBody @Valid CreateEventRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(groupId, currentUser().getId(), req));
    }

    @GetMapping("/groups/{groupId}/events")
    public ResponseEntity<List<EventSummaryResponse>> getGroupEvents(@PathVariable UUID groupId) {
        return ResponseEntity.ok(eventService.getGroupEvents(groupId, currentUser().getId()));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDetailResponse> getEventDetail(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEventDetail(eventId, currentUser().getId()));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId, currentUser().getId());
        return ResponseEntity.noContent().build();
    }
}
