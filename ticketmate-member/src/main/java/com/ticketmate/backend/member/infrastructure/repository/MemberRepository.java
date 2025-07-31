package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.core.constant.Role;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, UUID> {

  Optional<Member> findByUsername(String username);

  Optional<List<Member>> findAllByMemberType(MemberType memberType);

  void deleteAllByRole(Role role);

  // 팔로잉 수 + n
  @Modifying
  @Query("UPDATE Member m SET m.followingCount = m.followingCount + :delta WHERE m.memberId = :memberId")
  void updateFollowingCount(@Param("memberId") UUID memberId, long delta);

  // 팔로워 수 + n
  @Modifying
  @Query("UPDATE Member m SET m.followerCount = m.followerCount + :delta WHERE m.memberId = :memberId")
  void updateFollowerCount(@Param("memberId") UUID memberId, long delta);
}
