package com.ticketmate.backend.object.postgres.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.ApplicationRejectedType;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RejectionReason extends BasePostgresEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID rejectionReasonId;

    @Enumerated(EnumType.STRING)
    private ApplicationRejectedType applicationRejectedType;

    private String otherMemo; // 'applicationRejectedType' 이 '기타'일시 대리자가 작성 할 메모

    @OneToOne(fetch = FetchType.LAZY)
    private ApplicationForm applicationForm;
}
