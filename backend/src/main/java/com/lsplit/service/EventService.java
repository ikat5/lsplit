package com.lsplit.service;

import com.lsplit.dto.request.CreateEventRequest;
import com.lsplit.dto.response.EventDetailResponse;
import com.lsplit.dto.response.EventSummaryResponse;
import com.lsplit.dto.response.ExpenseItemResponse;
import com.lsplit.entity.Event;
import com.lsplit.entity.ExpenseItem;
import com.lsplit.entity.Group;
import com.lsplit.entity.GroupMember;
import com.lsplit.entity.User;
import com.lsplit.exception.ResourceNotFoundException;
import com.lsplit.exception.UnauthorizedException;
import com.lsplit.mapper.EventMapper;
import com.lsplit.mapper.ExpenseMapper;
import com.lsplit.repository.EventRepository;
import com.lsplit.repository.ExpenseItemRepository;
import com.lsplit.repository.GroupMemberRepository;
import com.lsplit.repository.GroupRepository;
import com.lsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final ExpenseMapper expenseMapper;

    public EventSummaryResponse createEvent(UUID groupId, UUID creatorId, CreateEventRequest req) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, creatorId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Event event = Event.builder()
                .group(group)
                .title(req.getTitle())
                .description(req.getDescription())
                .eventDate(req.getEventDate())
                .createdBy(creator)
                .build();
        eventRepository.save(event);
        return eventMapper.toEventSummaryResponse(event, 0, BigDecimal.ZERO);
    }

    public List<EventSummaryResponse> getGroupEvents(UUID groupId, UUID requesterId) {
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(groupId, requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        List<Event> events = eventRepository.findByGroup_Id(groupId);
        return events.stream().map(event -> {
            List<ExpenseItem> expenses = expenseItemRepository.findByEvent_Id(event.getId());
            BigDecimal total = expenses.stream()
                    .map(ExpenseItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return eventMapper.toEventSummaryResponse(event, expenses.size(), total);
        }).collect(Collectors.toList());
    }

    public EventDetailResponse getEventDetail(UUID eventId, UUID requesterId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        if (!groupMemberRepository.existsByGroup_IdAndUser_Id(event.getGroup().getId(), requesterId)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        List<ExpenseItem> expenses = expenseItemRepository.findByEvent_Id(eventId);
        List<ExpenseItemResponse> expenseResponses = expenses.stream()
                .map(expenseMapper::toExpenseItemResponse)
                .collect(Collectors.toList());
        BigDecimal total = expenses.stream()
                .map(ExpenseItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return eventMapper.toEventDetailResponse(event, expenseResponses, expenses.size(), total);
    }

    public void deleteEvent(UUID eventId, UUID requesterId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        UUID groupId = event.getGroup().getId();
        boolean isCreator = event.getCreatedBy().getId().equals(requesterId);
        GroupMember requesterMember = groupMemberRepository
                .findByGroup_IdAndUser_Id(groupId, requesterId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));
        boolean isAdmin = "ADMIN".equals(requesterMember.getRole());
        if (!isCreator && !isAdmin) {
            throw new UnauthorizedException("Only the creator or an admin can delete this event");
        }
        eventRepository.deleteById(eventId);
    }
}
