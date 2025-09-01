package com.ticketmate.backend.member.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.member.application.dto.response.MemberFollowResponse;
import com.ticketmate.backend.member.core.constant.MemberFollowSortField;
import com.ticketmate.backend.member.infrastructure.entity.MemberFollow;
import com.ticketmate.backend.member.infrastructure.entity.QMember;
import com.ticketmate.backend.member.infrastructure.entity.QMemberFollow;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberFollowRepositoryImpl implements MemberFollowRepositoryCustom {

  private static final QMemberFollow MEMBER_FOLLOW = QMemberFollow.memberFollow;
  private static final QMember CLIENT = new QMember("client");
  private static final QMember AGENT = new QMember("agent");

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<MemberFollowResponse> filteredMemberFollow(UUID clientId, Pageable pageable) {

    // 동적 WHERE 절 조합
    BooleanExpression whereClause = QueryDslUtil.allOf(
        QueryDslUtil.eqIfNotNull(CLIENT.memberId, clientId)
    );

    // contentQuery 생성
    JPAQuery<MemberFollowResponse> contentQuery = queryFactory
        .select(Projections.constructor(
            MemberFollowResponse.class,
            AGENT.nickname,
            AGENT.profileUrl,
            AGENT.followerCount
        ))
        .from(MEMBER_FOLLOW)
        .innerJoin(MEMBER_FOLLOW.follower, CLIENT)
        .innerJoin(MEMBER_FOLLOW.followee, AGENT)
        .where(whereClause);

    ComparableExpression<Long> agentFollowerCountExpression = Expressions.comparableTemplate(
        Long.class,
        "{0}",
        AGENT.followerCount
    );

    Map<String, ComparableExpression<?>> customSortMap = Collections.singletonMap(
        MemberFollowSortField.FOLLOWER_COUNT.getProperty(),
        agentFollowerCountExpression
    );

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        MemberFollow.class,
        MEMBER_FOLLOW.getMetadata().getName(),
        customSortMap
    );

    // Slice 페이징
    return QueryDslUtil.fetchSlice(contentQuery, pageable);
  }
}
