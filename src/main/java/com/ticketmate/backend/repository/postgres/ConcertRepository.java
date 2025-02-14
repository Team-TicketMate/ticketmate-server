package com.ticketmate.backend.repository.postgres;

import com.ticketmate.backend.object.postgres.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

    Boolean existsByConcertName(String concertName);

    // 콘서트 필터링
    @Query(value = """
            select c.*
            from ticket_mate.public.concert c
            left join ticket_mate.public.concert_hall ch on c.concert_hall_concert_hall_id = ch.concert_hall_id
            where (trim(:concertName) = '' or lower(concert_name) like lower(concat('%', :concertName, '%')))
            and (trim(:concertHallName) = '' or lower(concert_hall_name) like lower(concat('%', :concertHallName, '%')))
            and (:concertType = '' or :concertType = concert_type)
            and ((cast(:ticketPreOpenStartDate as timestamp) is null and cast(:ticketPreOpenEndDate as timestamp) is null)
                 or (cast(:ticketPreOpenStartDate as timestamp) is null or ticket_pre_open_date <= cast(:ticketPreOpenEndDate as timestamp))
                 or (cast(:ticketPreOpenEndDate as timestamp) is null or ticket_pre_open_date >= cast(:ticketPreOpenStartDate as timestamp))
                 or (ticket_pre_open_date >= cast(:ticketPreOpenStartDate as timestamp) and ticket_pre_open_date <= cast(:ticketPreOpenEndDate as timestamp)))
            and ((cast(:ticketOpenStartDate as timestamp) is null and cast(:ticketOpenEndDate as timestamp) is null)
                 or (cast(:ticketOpenStartDate as timestamp) is null or ticket_open_date <= cast(:ticketOpenEndDate as timestamp))
                 or (cast(:ticketOpenEndDate as timestamp) is null or ticket_open_date >= cast(:ticketOpenStartDate as timestamp))
                 or (ticket_open_date >= cast(:ticketOpenStartDate as timestamp) and ticket_open_date <= cast(:ticketOpenEndDate as timestamp)))
            and (:session = 0 or session = :session)
            and (:ticketReservationSite = '' or ticket_reservation_site = :ticketReservationSite)
            """,
            countQuery = """
            select count(c.*)
            from ticket_mate.public.concert c
            left join ticket_mate.public.concert_hall ch on c.concert_hall_concert_hall_id = ch.concert_hall_id
            where (trim(:concertName) = '' or lower(concert_name) like lower(concat('%', :concertName, '%')))
            and (trim(:concertHallName) = '' or lower(concert_hall_name) like lower(concat('%', :concertHallName, '%')))
            and (:concertType = '' or :concertType = concert_type)
            and ((cast(:ticketPreOpenStartDate as timestamp) is null and cast(:ticketPreOpenEndDate as timestamp) is null)
                 or (cast(:ticketPreOpenStartDate as timestamp) is null or ticket_pre_open_date <= cast(:ticketPreOpenEndDate as timestamp))
                 or (cast(:ticketPreOpenEndDate as timestamp) is null or ticket_pre_open_date >= cast(:ticketPreOpenStartDate as timestamp))
                 or (ticket_pre_open_date >= cast(:ticketPreOpenStartDate as timestamp) and ticket_pre_open_date <= cast(:ticketPreOpenEndDate as timestamp)))
            and ((cast(:ticketOpenStartDate as timestamp) is null and cast(:ticketOpenEndDate as timestamp) is null)
                 or (cast(:ticketOpenStartDate as timestamp) is null or ticket_open_date <= cast(:ticketOpenEndDate as timestamp))
                 or (cast(:ticketOpenEndDate as timestamp) is null or ticket_open_date >= cast(:ticketOpenStartDate as timestamp))
                 or (ticket_open_date >= cast(:ticketOpenStartDate as timestamp) and ticket_open_date <= cast(:ticketOpenEndDate as timestamp)))
            and (:session = 0 or session = :session)
            and (:ticketReservationSite = '' or ticket_reservation_site = :ticketReservationSite)
            """,
            nativeQuery = true)
    Page<Concert> filteredConcert(
            @Param("concertName") String concertName,
            @Param("concertHallName") String concertHallName,
            @Param("concertType") String concertType,
            @Param("ticketPreOpenStartDate") LocalDateTime ticketPreOpenStartDate,
            @Param("ticketPreOpenEndDate") LocalDateTime ticketPreOpenEndDate,
            @Param("ticketOpenStartDate") LocalDateTime ticketOpenStartDate,
            @Param("ticketOpenEndDate") LocalDateTime ticketOpenEndDate,
            @Param("session") int session,
            @Param("ticketReservationSite") String ticketReservationSite,
            Pageable pageable
            );
}
