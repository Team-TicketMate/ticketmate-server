package com.ticketmate.backend.repository.postgres.application;

import com.ticketmate.backend.object.postgres.application.ApplicationForm;
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
            where (:clientId = '' or CAST(af.client_member_id AS TEXT) = :clientId)
            and (:agentId = '' or CAST(af.agent_member_id AS TEXT) = :agentId)
            and (:concertId = '' or CAST(af.concert_concert_id AS TEXT) = :concertId)
            and (:requestCount = 0 or af.request_count = :requestCount)
            and (:applicationStatus = '' or af.application_status = :applicationStatus)
            """,
            countQuery = """
                    select count(*)
                    from application_form af
                    where (:clientId = '' or CAST(af.client_member_id AS TEXT) = :clientId)
                    and (:agentId = '' or CAST(af.agent_member_id AS TEXT) = :agentId)
                    and (:concertId = '' or CAST(af.concert_concert_id AS TEXT) = :concertId)
                    and (:requestCount = 0 or af.request_count = :requestCount)
                    and (:applicationStatus = '' or af.application_status = :applicationStatus)
                    """,
            nativeQuery = true)
    Page<ApplicationForm> filteredApplicationForm(
            @Param("clientId") String clientId,
            @Param("agentId") String agentId,
            @Param("concertId") String concertId,
            @Param("requestCount") Integer requestCount,
            @Param("applicationStatus") String applicationStatus,
            Pageable pageable
    );
}
