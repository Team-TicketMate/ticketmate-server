package com.ticketmate.backend.repository.postgres;

import com.ticketmate.backend.object.postgres.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    Boolean existsByUsername(String username);

    Optional<Member> findByUsername(String username);
}
