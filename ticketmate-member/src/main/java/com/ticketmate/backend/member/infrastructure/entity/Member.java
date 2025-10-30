package com.ticketmate.backend.member.infrastructure.entity;

import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.BIRTHDAY_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.BIRTHYEAR_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.NICKNAME_MAX_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.NICKNAME_MIN_LENGTH;
import static com.ticketmate.backend.member.core.constant.MemberInfoConstants.PHONE_MAX_LENGTH;

import com.ticketmate.backend.common.infrastructure.converter.NullIfBlankConverter;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.core.constant.AccountStatus;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.core.constant.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class Member extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID memberId;

  // 소셜 로그인 시 발급되는 ID
  private String socialLoginId;

  // 이메일 (DB 활성화 unique 제약조건 추가)
  private String username;

  // 닉네임 (DB 활성화 unique 제약조건 추가)
  @Size(min = NICKNAME_MIN_LENGTH, max = NICKNAME_MAX_LENGTH)
  @Column(length = NICKNAME_MAX_LENGTH)
  private String nickname;

  // 이름
  @Column(nullable = false)
  private String name;

  // 소셜 플랫폼
  @Enumerated(EnumType.STRING)
  private SocialPlatform socialPlatform;

  // 생일
  @Column(length = BIRTHDAY_LENGTH)
  private String birthDay;

  // 출생연도
  @Column(length = BIRTHYEAR_LENGTH)
  private String birthYear;

  // 전화번호 (DB 활성화 unique 제약조건 추가)
  @Column(length = PHONE_MAX_LENGTH)
  private String phone;

  // 프로필 이미지
  private String profileImgStoredPath;

  // 성별
  private String gender;

  // 한줄 소개
  @Column(length = 50)
  @Convert(converter = NullIfBlankConverter.class)
  private String introduction;

  // 권한 (유저, 관리자)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Role role = Role.ROLE_USER;

  // 회원 종류 (대리인, 구매자)
  @Enumerated(EnumType.STRING)
  private MemberType memberType;

  // 계정 상태 (활성, 삭제)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private AccountStatus accountStatus = AccountStatus.ACTIVE;

  // 첫 로그인 여부
  @Builder.Default
  private Boolean isFirstLogin = true;

  // 마지막 로그인 시간
  @Column(columnDefinition = "TIMESTAMPTZ(0)")
  private Instant lastLoginTime;

  // 본인인증 여부
  @Builder.Default
  @Column(nullable = false)
  private boolean isPhoneNumberVerified = false;

  // 기본 프로필 설정 여부
  @Builder.Default
  @Column(nullable = false)
  private boolean isInitialProfileSet = false;

  // 팔로잉 수 (내가 팔로잉하는 수)
  @Builder.Default
  @Column(nullable = false)
  private long followingCount = 0L;

  // 팔로워 수 (나를 팔로우하는 수)
  @Builder.Default
  @Column(nullable = false)
  private long followerCount = 0L;

  /**
   * 관리자
   */
  private String password;

  @Builder.Default
  @Column(nullable = false)
  private boolean totpEnabled = false;

  private String totpSecret;
}
