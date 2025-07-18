package com.ticketmate.backend.domain.member.repository;

import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.domain.entity.MemberFollow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFollowRepository extends JpaRepository<MemberFollow, UUID> {

  boolean existsByFollowerAndFollowee(Member follower, Member followee);

  void deleteByFollowerAndFollowee(Member follower, Member followee);
}
