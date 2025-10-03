package com.ticketmate.backend.member.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.crypto.infrastructure.converter.AesGcmConverter;
import com.ticketmate.backend.member.core.constant.BankCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AgentBankAccount extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID agentBankAccountId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member agent; // 대리인

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BankCode bankCode;  // 은행 코드

  @Column(nullable = false)
  private String bankName;  // 은행 이름

  @Column(nullable = false, length = 64)
  private String accountHolder;  // 예금주

  @Column(nullable = false, length = 256)
  @Convert(converter = AesGcmConverter.class)
  private String accountNumberEnc;  // 전체 계좌번호(암호화 저장)

  @Column(nullable = false)
  private boolean primaryAccount; // 대표계좌 여부
}
