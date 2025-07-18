package com.ticketmate.backend.global.validator.member;

import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.MemberFollowRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemberFollowValidator {

  private final Member follower;
  private final Member followee;

  private MemberFollowValidator(Member follower, Member followee) {
    this.follower = follower;
    this.followee = followee;
  }

  public static MemberFollowValidator of(Member follower, Member followee) {
    return new MemberFollowValidator(follower, followee);
  }

  /**
   * 자기 자신 팔로우 검증
   */
  public MemberFollowValidator validateSelfFollow() {
    if (follower.getMemberId().equals(followee.getMemberId())) {
      log.error("자기 자신을 팔로우 할 수 없습니다. followerId: {}, followeeId: {}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
    }
    return this;
  }

  /**
   * 동일한 MemberType 팔로우 검증
   */
  public MemberFollowValidator validateMemberTypeNotSame() {
    if (follower.getMemberType().equals(followee.getMemberType())) {
      log.error("의뢰인 <-> 대리인 간 팔로우만 가능합니다. followerType: {}, followeeType: {}",
          follower.getMemberType(), followee.getMemberType());
      throw new CustomException(ErrorCode.SAME_MEMBER_TYPE_FOLLOW_NOT_ALLOWED);
    }
    return this;
  }

  /**
   * 이미 팔로우된 상태 검증
   */
  public MemberFollowValidator validateDuplicateFollow(MemberFollowRepository repository) {
    if (repository.existsByFollowerAndFollowee(follower, followee)) {
      log.error("이미 필로우한 회원입니다. followerId={}, followeeId={}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.DUPLICATE_FOLLOW_NOT_ALLOWED);
    }
    return this;
  }

  /**
   * 언팔로우 가능 여부 검증
   */
  public MemberFollowValidator validateUnfollowAble(MemberFollowRepository repository) {
    if (!repository.existsByFollowerAndFollowee(follower, followee)) {
      log.error("팔로우 되어있지 않은 회원입니다. 언팔로우 불가. followerId={}, followeeId={}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.UNFOLLOW_NOT_ALLOWED);
    }
    return this;
  }
}
