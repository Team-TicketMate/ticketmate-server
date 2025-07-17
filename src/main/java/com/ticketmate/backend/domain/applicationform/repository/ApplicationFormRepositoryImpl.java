package com.ticketmate.backend.domain.applicationform.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormSortField;
import com.ticketmate.backend.domain.applicationform.domain.constant.ApplicationFormStatus;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.QApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.QApplicationFormDetail;
import com.ticketmate.backend.domain.concert.domain.entity.QConcert;
import com.ticketmate.backend.domain.member.domain.entity.QMember;
import com.ticketmate.backend.global.util.database.QueryDslUtil;
import java.util.Collections;
import java.util.Map;
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

  private static final QApplicationForm APPLICATION_FORM = QApplicationForm.applicationForm;
  private static final QApplicationFormDetail APPLICATION_FORM_DETAIL = QApplicationFormDetail.applicationFormDetail;
  private static final QMember CLIENT = new QMember("clientAlias");
  private static final QMember AGENT = new QMember("agentAlias");
  private static final QConcert CONCERT = QConcert.concert;

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

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.eqIfNotNull(APPLICATION_FORM.client.memberId, clientId),
        QueryDslUtil.eqIfNotNull(APPLICATION_FORM.agent.memberId, agentId),
        QueryDslUtil.eqIfNotNull(APPLICATION_FORM.concert.concertId, concertId),
        (applicationFormStatusSet != null && !applicationFormStatusSet.isEmpty())
            ? APPLICATION_FORM.applicationFormStatus.in(applicationFormStatusSet)
            : null
    );

    // contentQuery 생성
    JPAQuery<ApplicationFormFilteredResponse> contentQuery = queryFactory
        .select(Projections.constructor(
            ApplicationFormFilteredResponse.class,
            APPLICATION_FORM.applicationFormId,
            CONCERT.concertName,
            CONCERT.concertThumbnailUrl,
            CLIENT.nickname,
            AGENT.nickname,
            APPLICATION_FORM.updatedDate,
            APPLICATION_FORM.applicationFormStatus,
            APPLICATION_FORM.ticketOpenType
        ))
        .from(APPLICATION_FORM)
        .innerJoin(APPLICATION_FORM.client, CLIENT)
        .innerJoin(APPLICATION_FORM.agent, AGENT)
        .innerJoin(APPLICATION_FORM.concert, CONCERT)
        .innerJoin(APPLICATION_FORM_DETAIL).on(APPLICATION_FORM_DETAIL.applicationForm.eq(APPLICATION_FORM))
        .where(whereClause)
        .groupBy(
            APPLICATION_FORM.applicationFormId,
            CONCERT.concertName,
            CONCERT.concertThumbnailUrl,
            CLIENT.nickname,
            AGENT.nickname,
            APPLICATION_FORM.updatedDate,
            APPLICATION_FORM.applicationFormStatus,
            APPLICATION_FORM.ticketOpenType
        );

    ComparableExpression<Integer> requestCountMaxExpression = Expressions.comparableTemplate(
        Integer.class,
        "max({0})",
        APPLICATION_FORM_DETAIL.requestCount
    );

    // enum.property -> 표현식 매핑
    Map<String, ComparableExpression<?>> customSortMap = Collections.singletonMap(
        ApplicationFormSortField.REQUEST_COUNT.getProperty(),
        requestCountMaxExpression
    );

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        ApplicationForm.class,
        APPLICATION_FORM.getMetadata().getName(),
        customSortMap
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(APPLICATION_FORM.count())
        .from(APPLICATION_FORM)
        .where(whereClause);

    // 페이징 처리 및 결과 반환
    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }
}
