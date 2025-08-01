package com.ticketmate.backend.member.application.mapper;

import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {

  // Member -> MemberInfoResponse (DTO)
  MemberInfoResponse toMemberInfoResponse(Member member);

}
