package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.AgentBankAccount;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentBankAccountRepository extends JpaRepository<AgentBankAccount, UUID> {
  long countByAgent(Member agent);

  boolean existsByAgentAndPrimaryAccountTrue(Member agent);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      UPDATE agent_bank_account
      SET primary_account = false
      WHERE agent_member_id = :agentId AND primary_account = true
      """,
      nativeQuery = true)
  void demoteAllPrimaryAccount(@Param("agentId") UUID agentId);

  List<AgentBankAccount> findAllByAgent(Member member);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value = """
          UPDATE agent_bank_account
          SET primary_account = (agent_bank_account_id = :agentBankAccountId)
          WHERE agent_member_id = :agentId
          """,
      nativeQuery = true
  )
  int setPrimaryAccountExclusively(@Param("agentId") UUID agentId, @Param("agentBankAccountId") UUID agentBankAccountId);

  Optional<AgentBankAccount> findFirstByAgentOrderByCreatedDateAsc(Member agent);
}
