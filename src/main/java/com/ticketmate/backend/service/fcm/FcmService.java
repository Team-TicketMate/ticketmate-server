package com.ticketmate.backend.service.fcm;

import com.ticketmate.backend.object.dto.fcm.request.FcmTokenSaveRequest;
import com.ticketmate.backend.object.dto.fcm.response.FcmTokenSaveResponse;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.redis.FcmToken;
import com.ticketmate.backend.repository.redis.FcmTokenRepository;
import com.ticketmate.backend.util.common.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    private final FcmTokenRepository fcmTokenRepository;
    private final EntityMapper mapper;

    /**
     * RedisHash에 FCM 토큰이 저장되는 로직입니다.
     */
    @Transactional
    public FcmTokenSaveResponse saveFcmToken(FcmTokenSaveRequest request, Member member) {
        // DTO -> 엔티티
        FcmToken fcmToken = FcmToken.builder()
                .fcmToken(request.getFmcToken())
                .memberId(member.getMemberId())
                .memberPlatform(request.getMemberPlatform())
                .build();

        // 같은 키(memberId-platform)에 대해 호출 시 기존 데이터가 자동으로 덮어써짐 (사용자의 기기마다 토큰값은 유일하게 설계)
        fcmTokenRepository.save(fcmToken);

        log.debug("토큰 저장 완료");
        log.debug("사용자 ID : {}", fcmToken.getMemberId());
        log.debug("사용자 기기 : {}", fcmToken.getMemberPlatform());

        return mapper.toFcmTokenSaveResponse(fcmToken);
    }
}
