package com.ticketmate.backend.domain.member.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.domain.member.domain.constant.AccountStatus;
import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.constant.Role;
import com.ticketmate.backend.domain.member.domain.constant.SocialPlatform;
import com.ticketmate.backend.global.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID memberId;

  // 소셜 로그인 시 발급되는 ID
  @Column(nullable = false)
  private String socialLoginId;

  // 이메일
  @Column(unique = true)
  private String username;

  // 닉네임
  @Column(unique = true)
  private String nickname;

  // 이름
  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SocialPlatform socialPlatform;

  // 생일
  private String birthDay;

  // 출생연도
  private String birthYear;

  // 전화번호
  private String phone;

  // 프로필 이미지
  private String profileUrl;

  // 성별
  private String gender;

  // 권한 (유저, 관리자)
  @Enumerated(EnumType.STRING)
  private Role role;

  // 회원 종류 (대리인, 구매자)
  @Enumerated(EnumType.STRING)
  private MemberType memberType;

  // 계정 상태 (활성, 삭제)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AccountStatus accountStatus = AccountStatus.ACTIVE_ACCOUNT;

  // 첫 로그인 여부 (DB 저장 X)
  @Builder.Default
//    @Transient
  private Boolean isFirstLogin = true;

  // 마지막 로그인 시간
  private LocalDateTime lastLoginTime;
}
