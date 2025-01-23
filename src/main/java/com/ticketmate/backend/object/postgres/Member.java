package com.ticketmate.backend.object.postgres;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

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
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID memberId;

  // 이메일
  @Column(unique = true)
  private String username;

  // 비밀번호
  private String password;

  // 닉네임
  @Column(unique = true)
  private String nickname;

  // 생년월일 (ex.19980114)
  @Column(length = 8)
  private String birth;

  // 전화번호
  @Column(length = 11)
  private String phone;

  // 프로필 이미지
  private String profileUrl;

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

  // 마지막 로그인 시간
  private LocalDateTime lastLoginTime;
}
