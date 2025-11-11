package com.ticketmate.backend.member.application.dto.response;

import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.vo.Phone;
import java.util.UUID;

public record MemberInfoResponse(
  UUID memberId, // 회원 PK
  String username, // 이메일
  String nickname, // 닉네임
  String name, // 이름
  String birthDay, // 생일
  String birthYear, // 출생연도
  Phone phone, // 전화번호
  String profileUrl, // 프로필 이미지
  String gender, // 성별
  String introduction, // 한줄 소개
  MemberType memberType, // 회원 종류 (대리인, 구매자)
  long followingCount, // 팔로잉 수 (내가 팔로잉하는 수)
  long followerCount // 팔로워 수 (나를 팔로우하는 수)
) {

}
