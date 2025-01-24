package com.ticketmate.backend.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@ToString
@SuperBuilder
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BasePostgresEntity {

    // 생성일
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // 수정일
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedDate;

    // 삭제여부
    @Builder.Default
    private boolean isDeleted = false;
}
