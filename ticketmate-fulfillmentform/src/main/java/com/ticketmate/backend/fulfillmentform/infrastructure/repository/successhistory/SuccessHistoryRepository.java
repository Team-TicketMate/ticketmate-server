package com.ticketmate.backend.fulfillmentform.infrastructure.repository.successhistory;

import com.ticketmate.backend.fulfillmentform.infrastructure.entity.SuccessHistory;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuccessHistoryRepository extends JpaRepository<SuccessHistory, UUID> {

  boolean existsByFulfillmentForm_FulfillmentFormId(UUID fulfillmentFormId);

  @Query("""
      select
        ff.fulfillmentFormId as fulfillmentId,
        c.concertName as concertName,
        c.concertThumbnailStoredPath as concertThumbnailStoredPath,
        c.concertType as concertType,
        sh.createdDate as createDate,
        cl.nickname as clientNickname,
        r.reviewId as reviewId,
        r.rating as reviewRating
      from SuccessHistory sh
        join sh.fulfillmentForm ff
        join ff.concert c
        join ff.client cl
        left join Review r on r.fulfillmentForm = ff
      where ff.agent.memberId = :agentMemberId
    """)
  Slice<SuccessHistoryRow> findSuccessHistoryList(@Param("agentMemberId") UUID agentMemberId, Pageable pageable);
}
