package com.ticketmate.backend.repository.postgres.application;

import com.ticketmate.backend.object.constants.ApplicationFormStatus;
import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.concert.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, UUID> {

    @Query(value = """
            select *
            from application_form af
            where (:clientId is null or af.client_member_id = :clientId)
            and (:agentId is null or af.agent_member_id = :agentId)
            and (:concertId is null or af.concert_concert_id = :concertId)
            and (:applicationFormStatus = '' or af.application_form_status = :applicationFormStatus)
            """,
            countQuery = """
                    select count(*)
                    from application_form af
                    where (:clientId is null or af.client_member_id = :clientId)
                    and (:agentId is null or af.agent_member_id = :agentId)
                    and (:concertId is null or af.concert_concert_id = :concertId)
                    and (:applicationFormStatus = '' or af.application_form_status = :applicationFormStatus)
                    """,
            nativeQuery = true)
    Page<ApplicationForm> filteredApplicationForm(
            @Param("clientId") UUID clientId,
            @Param("agentId") UUID agentId,
            @Param("concertId") UUID concertId,
            @Param("applicationFormStatus") String applicationFormStatus,
            Pageable pageable
    );

    // 이미 의뢰인이 대리인에게 해당 공연에 대한 선예매/일반예매 신청서를 보냈는지 검증
    boolean existsByClientMemberIdAndAgentMemberIdAndConcertConcertIdAndTicketOpenType(UUID clientID, UUID agentId, UUID concertId, TicketOpenType ticketOpenType);

    // 이미 동일한 의뢰인에 대해서 신청서를 수락했는지 검증 (똑같은 공연 + 똑같은 선예매/일반예매 + 수락상태[이미 수락했는 뜻])
    boolean existsByConcertAndClientAndTicketOpenTypeAndApplicationFormStatus(Concert concert, Member client, TicketOpenType ticketOpenType, ApplicationFormStatus applicationFormStatus);
}
