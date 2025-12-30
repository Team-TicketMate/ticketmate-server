package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.ticketmate.backend.applicationform.core.constant.ApplicationFormStatus;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.concert.core.constant.TicketOpenType;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, UUID> {

  // 대리인PK, 의뢰인PK, 공연PK, 공연 오픈 타입으로 신청서 중복 판단 (대기, 승인 상태의 신청서 대상)
  boolean existsByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenTypeAndApplicationFormStatusIn(
    UUID clientId,
    UUID agentId,
    UUID concertId,
    TicketOpenType ticketOpenType,
    Set<ApplicationFormStatus> duplicateCheckStatusSet
  );
}
