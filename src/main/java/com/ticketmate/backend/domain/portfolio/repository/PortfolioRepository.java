package com.ticketmate.backend.domain.portfolio.repository;

import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {

  @Query(value = """
      select p.*
      from portfolio p
      left join member m on p.member_member_id = m.member_id
      where (trim(:username) = '' or lower(m.username) like lower(concat('%', :username, '%')))
      and (trim(:nickname) = '' or lower(m.nickname) like lower(concat('%', :nickname, '%')))
      and (trim(:name) = '' or lower(m.name) like lower(concat('%', :name, '%')))
      and (:portfolioType = '' or p.portfolio_type = :portfolioType)
      """, countQuery = """
      select count(p.portfolio_id)
      from portfolio p
      left join member m on p.member_member_id = m.member_id
      where (trim(:username) = '' or lower(m.username) like lower(concat('%', :username, '%')))
      and (trim(:nickname) = '' or lower(m.nickname) like lower(concat('%', :nickname, '%')))
      and (trim(:name) = '' or lower(m.name) like lower(concat('%', :name, '%')))
      and (:portfolioType = '' or p.portfolio_type = :portfolioType)
      """, nativeQuery = true)
  Page<Portfolio> filteredPortfolio(
      @Param("username") String username,
      @Param("nickname") String nickname,
      @Param("name") String name,
      @Param("portfolioType") String portfolioType,
      Pageable pageable);
}