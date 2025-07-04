package com.ticketmate.backend.domain.applicationform.repository;

import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, UUID> {

  // 이미 의뢰인이 대리인에게 해당 공연에 대한 선예매/일반예매 신청서를 보냈는지 검증
  boolean existsByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(UUID clientID, UUID agentId, UUID concertId, TicketOpenType ticketOpenType);
}
