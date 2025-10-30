package com.ticketmate.backend.member.infrastructure.repository;

import com.ticketmate.backend.member.infrastructure.entity.MemberWithdrawalHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberWithdrawalHistoryRepository extends JpaRepository<MemberWithdrawalHistory, UUID> {

}
