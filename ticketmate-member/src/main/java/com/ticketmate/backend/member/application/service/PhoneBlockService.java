package com.ticketmate.backend.member.application.service;

import static com.ticketmate.backend.member.infrastructure.constant.BlockConstants.WITHDRAW_BLOCK_DURATION;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.member.core.constant.BlockType;
import com.ticketmate.backend.member.core.vo.Phone;
import com.ticketmate.backend.member.infrastructure.entity.PhoneBlock;
import com.ticketmate.backend.member.infrastructure.repository.PhoneBlockRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhoneBlockService {

  private final PhoneBlockRepository phoneBlockRepository;

  /**
   * 전화번호 차단 활성화
   * 이미 활성화된 차단의 경우 더 긴 기한으로 업데이트
   */
  @Transactional
  public void executePhoneBlock(Phone phone, BlockType blockType) {
    Instant newBlockedUntil = calculateBlockedUntil(blockType);

    phoneBlockRepository.findByPhone(phone)
        .ifPresentOrElse(
            phoneBlock -> updatePhoneBlock(phoneBlock, blockType, newBlockedUntil),
            () -> createPhoneBlock(phone, blockType, newBlockedUntil)
        );
  }

  /**
   * 전화번호 차단 여부
   */
  @Transactional(readOnly = true)
  public void ensurePhoneNotBlocked(Phone phone) {
    if (phone == null) {
      log.error("차단 여부 조회 오류 발생: 요청 전화번호가 비어있습니다.");
      throw new CustomException(ErrorCode.PHONE_REQUIRED);
    }

    phoneBlockRepository.findByPhone(phone)
        .filter(PhoneBlock::isCurrentlyBlocked)
        .ifPresent(phoneBlock -> {
          log.warn("차단된 전화번호: {}, 차단유형: {}, 차단기간: {}",
              phoneBlock.getPhone(), phoneBlock.getBlockType(), phoneBlock.getBlockedUntil());
          throw new CustomException(ErrorCode.PHONE_BLOCKED);
        });
  }

  // BlockType에 따른 차단 기간 반환
  private Instant calculateBlockedUntil(BlockType blockType) {
    Instant now = TimeUtil.now();

    return switch (blockType) {
      case WITHDRAWAL -> now.plus(WITHDRAW_BLOCK_DURATION);
      case TEMP_BAN -> null; // TODO: 임시 밴 로직 수정
      case PERMANENT_BAN -> null;
    };
  }

  // 새로운 PhoneBlock 엔티티 생성 및 저장
  private void createPhoneBlock(Phone phone, BlockType blockType, Instant blockedUntil) {
    phoneBlockRepository.save(PhoneBlock.create(phone, blockType, blockedUntil));
    log.debug("새로운 전화번호: {} 차단 활성화. BlockType: {}, 차단기한: {}", phone, blockType, blockedUntil);
  }

  // 기존의 PhoneBlock 데이터 처리
  private void updatePhoneBlock(PhoneBlock phoneBlock, BlockType blockType, Instant blockedUntil) {
    // 차단 만료로 현재 차단중이 아니면 요청 값으로 설정
    if (!phoneBlock.isCurrentlyBlocked()) {
      updateBlockTypeAndBlockedUntil(phoneBlock, blockType, blockedUntil);
      log.debug("차단 만료된 전화번호: {} 차단 재활성화. BlockType: {}, 차단기한: {}", phoneBlock.getPhone(), blockType, blockedUntil);
      return;
    }
    // 이미 영구밴인 경우
    if (phoneBlock.getBlockType() == BlockType.PERMANENT_BAN) {
      log.debug("이미 영구 차단된 전화번호입니다: {}", phoneBlock.getPhone());
      return;
    }
    // 영구밴 요청 시
    if (blockType == BlockType.PERMANENT_BAN) {
      updateBlockTypeAndBlockedUntil(phoneBlock, blockType, blockedUntil);
      log.debug("전화번호: {} 영구 차단 활성화. BlockType: {}, 차단기한: {}", phoneBlock.getPhone(), blockType, blockedUntil);
      return;
    }
    // 더 긴 차단 기간으로 업데이트
    if (isLongerBlockedUntilPeriod(phoneBlock, blockedUntil)) {
      updateBlockTypeAndBlockedUntil(phoneBlock, blockType, blockedUntil);
      log.debug("차단된 전화번호: {} 차단 갱신 완료. BlockType: {}, 차단기한: {}", phoneBlock.getPhone(), blockType, blockedUntil);
    }
  }

  // blockType, blockedUntil 업데이트
  private void updateBlockTypeAndBlockedUntil(PhoneBlock phoneBlock, BlockType blockType, Instant blockedUntil) {
    phoneBlock.setBlockType(blockType);
    phoneBlock.setBlockedUntil(blockedUntil);
    phoneBlockRepository.save(phoneBlock);
  }

  // 기존 차단기한과 새로운 차단기한 비교
  private boolean isLongerBlockedUntilPeriod(PhoneBlock phoneBlock, Instant blockedUntil) {
    return phoneBlock.getBlockedUntil() != null
           && blockedUntil != null
           && phoneBlock.getBlockedUntil().isBefore(blockedUntil);
  }
}
