package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MemberFollowRepositoryCustom {

  // 팔로우 필터링 조회
  Slice<MemberFollowResponse> filteredMemberFollow(
      UUID clientId,
      Pageable pageable
  );

}
