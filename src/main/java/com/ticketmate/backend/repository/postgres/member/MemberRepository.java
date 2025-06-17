package com.ticketmate.backend.repository.postgres.member;

import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.postgres.Member.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

  Optional<Member> findByUsername(String username);

  Optional<List<Member>> findAllByMemberType(MemberType memberType);

  void deleteAllByRole(Role role);
}
