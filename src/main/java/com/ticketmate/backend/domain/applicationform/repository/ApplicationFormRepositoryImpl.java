package com.ticketmate.backend.domain.applicationform.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.QApplicationForm;
import com.ticketmate.backend.domain.concert.domain.entity.QConcert;
import com.ticketmate.backend.domain.member.domain.entity.QMember;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplicationFormRepositoryImpl implements ApplicationFormRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 신청서 필터링 조회
   *
   * @param clientId                 특정 의뢰인이 작성한 신청서
   * @param agentId                  특정 대리인에게 작성된 신청서
   * @param concertId                특정 공연에 대해 작성된 신청서
   * @param applicationFormStatusSet 신청서 상태 Set (다중 선택 가능)
   */
  @Override
  public Page<ApplicationFormFilteredResponse> filteredApplicationForm(
      UUID clientId,
      UUID agentId,
      UUID concertId,
      Set<ApplicationFormStatus> applicationFormStatusSet,
      Pageable pageable) {

    QApplicationForm applicationForm = QApplicationForm.applicationForm;
    QMember client = new QMember("clientAlias");
    QMember agent = new QMember("agentAlias");
    QConcert concert = QConcert.concert;

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.eqIfNotNull(applicationForm.client.memberId, clientId),
        QueryDslUtil.eqIfNotNull(applicationForm.agent.memberId, agentId),
        QueryDslUtil.eqIfNotNull(applicationForm.concert.concertId, concertId),
        (applicationFormStatusSet != null && !applicationFormStatusSet.isEmpty())
            ? applicationForm.applicationFormStatus.in(applicationFormStatusSet)
            : null
    );

    // contentQuery 생성
    JPAQuery<ApplicationFormFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(
            ApplicationFormFilteredResponse.class,
            applicationForm.applicationFormId,
            concert.concertName,
            concert.concertThumbnailUrl,
            client.nickname,
            agent.nickname,
            applicationForm.updatedDate,
            applicationForm.applicationFormStatus,
            applicationForm.ticketOpenType
        ))
        .from(applicationForm)
        .innerJoin(applicationForm.client, client)
        .innerJoin(applicationForm.agent, agent)
        .innerJoin(applicationForm.concert, concert)
        .where(whereClause);

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        ApplicationForm.class,
        applicationForm.getMetadata().getName()
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(applicationForm.count())
        .from(applicationForm)
        .where(whereClause);

    // 페이징 처리 및 결과 반환
    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
