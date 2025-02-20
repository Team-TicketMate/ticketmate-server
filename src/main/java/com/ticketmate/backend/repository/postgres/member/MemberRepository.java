package com.ticketmate.backend.repository.postgres.member;

import com.ticketmate.backend.object.postgres.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    Boolean existsByUsername(String username);

    Member findByUsername(String username);
}
