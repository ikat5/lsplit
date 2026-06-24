package com.lsplit.repository;

import com.lsplit.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByGroup_Id(UUID groupId);
}
