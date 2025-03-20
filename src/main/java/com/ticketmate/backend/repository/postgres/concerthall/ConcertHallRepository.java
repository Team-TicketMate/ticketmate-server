package com.ticketmate.backend.repository.postgres.concerthall;

import com.ticketmate.backend.object.constants.City;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConcertHallRepository extends JpaRepository<ConcertHall, UUID> {

    Boolean existsByConcertHallName(String concertHallName);

    Optional<ConcertHall> findByConcertHallName(String concertHallName);

    @Query(value = """
            select *
            from concert_hall
            where (trim(:concertHallName) = '' or lower(concert_hall_name) like lower(concat('%', :concertHallName, '%')))
            and (:city = '' or :city = city)
            """,
            countQuery = """
                    select count(*)
                    from concert_hall
                    where (trim(:concertHallName) = '' or lower(concert_hall_name) like lower(concat('%', :concertHallName, '%')))
                    and (trim(:city) = '' or :city = city)
                    """,
            nativeQuery = true)
    Page<ConcertHall> filteredConcertHall(
            @Param("concertHallName") String concertHallName,
            @Param("city") City city,
            Pageable pageable
    );

}
