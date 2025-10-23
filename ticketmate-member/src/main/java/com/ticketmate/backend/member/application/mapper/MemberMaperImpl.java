package com.ticketmate.backend.member.application.mapper;

import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.storage.core.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MemberMaperImpl implements MemberMapper {

  private final StorageService storageService;

  @Override
  public MemberInfoResponse toMemberInfoResponse(Member member) {
    String profileUrl = storageService.generatePublicUrl(member.getProfileImgStoredPath());
    return new MemberInfoResponse(
        member.getMemberId(),
        member.getUsername(),
        member.getNickname(),
        member.getName(),
        member.getBirthDay(),
        member.getBirthYear(),
        member.getPhone(),
        profileUrl,
        member.getGender(),
        member.getIntroduction(),
        member.getMemberType(),
        member.getFollowingCount(),
        member.getFollowerCount()
    );

  }
}
