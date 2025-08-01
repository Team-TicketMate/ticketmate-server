package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, UUID> {

  // 대리인PK, 의뢰인PK, 공연PK, 공연 오픈 타입으로 신청서 조회
  Optional<ApplicationForm> findByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(UUID clientID, UUID agentId, UUID concertId, TicketOpenType ticketOpenType);
}
