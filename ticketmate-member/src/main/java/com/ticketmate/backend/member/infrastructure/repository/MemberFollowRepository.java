package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.entity.MemberFollow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFollowRepository extends JpaRepository<MemberFollow, UUID> {

  boolean existsByFollowerAndFollowee(Member follower, Member followee);

  void deleteByFollowerAndFollowee(Member follower, Member followee);
}
