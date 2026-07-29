package com.lsplit.repository;

import com.lsplit.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMember.GroupMemberId> {

    List<GroupMember> findByGroup_Id(UUID groupId);

    List<GroupMember> findByUser_Id(UUID userId);

    Optional<GroupMember> findByGroup_IdAndUser_Id(UUID groupId, UUID userId);

    boolean existsByGroup_IdAndUser_Id(UUID groupId, UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
}
