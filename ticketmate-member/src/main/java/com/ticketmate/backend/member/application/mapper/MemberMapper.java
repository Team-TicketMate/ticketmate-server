package com.ticketmate.backend.member.application.mapper;

import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

  // Member -> MemberInfoResponse (DTO)
  @Mapping(target = "introduction", source = "introduction", defaultValue = "")
  MemberInfoResponse toMemberInfoResponse(Member member);

}
