package com.ticketmate.backend.domain.concerthall.repository;

import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConcertHallRepository extends JpaRepository<ConcertHall, UUID> {

  Boolean existsByConcertHallName(String concertHallName);

  Optional<ConcertHall> findByConcertHallName(String concertHallName);

  @Query(value = """
      select *
      from concert_hall ch
      where (trim(:concertHallName) = '' or lower(ch.concert_hall_name) like lower(concat('%', :concertHallName, '%')))
      and (:city = '' or :city = ch.city)
      """,
      countQuery = """
          select count(*)
          from concert_hall ch
          where (trim(:concertHallName) = '' or lower(ch.concert_hall_name) like lower(concat('%', :concertHallName, '%')))
          and (:city = '' or :city = ch.city)
          """,
      nativeQuery = true)
  Page<ConcertHall> filteredConcertHall(
      @Param("concertHallName") String concertHallName,
      @Param("city") String city,
      Pageable pageable
  );

}
