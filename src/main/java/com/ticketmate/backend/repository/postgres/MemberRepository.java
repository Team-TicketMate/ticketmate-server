package com.ticketmate.backend.repository.postgres;

import com.ticketmate.backend.object.postgres.Member;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

}
