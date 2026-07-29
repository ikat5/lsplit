package com.lsplit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private String role = "MEMBER";

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "group_id")
        private UUID groupId;

        @Column(name = "user_id")
        private UUID userId;
    }
}
