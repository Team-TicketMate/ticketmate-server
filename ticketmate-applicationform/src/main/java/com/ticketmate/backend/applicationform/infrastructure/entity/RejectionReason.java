package com.ticketmate.backend.applicationform.infrastructure.entity;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormRejectedType;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class RejectionReason extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID rejectionReasonId;

  @OneToOne(fetch = FetchType.LAZY)
  private ApplicationForm applicationForm;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplicationFormRejectedType applicationFormRejectedType;

  private String otherMemo; // 'applicationRejectedType' 이 '기타'일시 대리자가 작성 할 메모
}
