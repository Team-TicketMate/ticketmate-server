package com.ticketmate.backend.member.application.mapper;

import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.infrastructure.entity.Member;

public interface MemberMapper {

  // Member -> MemberInfoResponse (DTO)
  MemberInfoResponse toMemberInfoResponse(Member member);
}
