package com.ticketmate.backend.member.infrastructure.entity;

import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.PHONE_MAX_LENGTH;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.member.core.constant.BlockType;
import com.ticketmate.backend.member.core.vo.Phone;
import com.ticketmate.backend.member.infrastructure.converter.PhoneJpaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
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
public class PhoneBlock extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID phoneBlockId;

  @Column(nullable = false, unique = true, length = PHONE_MAX_LENGTH)
  @Convert(converter = PhoneJpaConverter.class)
  private Phone phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BlockType blockType;

  // TODO: 추후 밴 기능 개발 후 PhoneBlockHistory.java 를 통해 제제내역 히스토리 기능 추가 + 히스토리 내부 BlockedReason 필드 추가

  @Column(columnDefinition = "TIMESTAMPTZ(0)")
  private Instant blockedUntil;

  public static PhoneBlock create(Phone phone, BlockType blockType, Instant blockedUntil) {
    return PhoneBlock.builder()
        .phone(phone)
        .blockType(blockType)
        .blockedUntil(blockedUntil)
        .build();
  }

  // 현재 시점 기준으로 차단 여부
  public boolean isCurrentlyBlocked() {
    return isBlockedAt(TimeUtil.now());
  }

  // 특정 시점 기준으로 차단 여부
  public boolean isBlockedAt(Instant when) {
    if (blockType == BlockType.PERMANENT_BAN) {
      return true;
    }
    return blockedUntil != null && when != null && blockedUntil.isAfter(when);
  }
}
