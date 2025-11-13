package com.ticketmate.backend.admin.report.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.admin.report.application.dto.view.ReportInfo;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import com.ticketmate.backend.report.infrastructure.entity.QReport;
import com.ticketmate.backend.report.infrastructure.entity.Report;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

  private static final QReport REPORT = QReport.report;
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ReportInfo> filteredReports(Pageable pageable){
    // TODO: 필터링 로직, 반환값 추가 등 수정 필요
    JPAQuery<ReportInfo> contentQuery = queryFactory
        .select(Projections.constructor(ReportInfo.class,
            REPORT.reportId,
            REPORT.reporter.memberId,
            REPORT.reportedMember.memberId,
            REPORT.reportReason,
            Expressions.constant(""),
            REPORT.reportStatus,
            REPORT.createdDate
        ))
        .from(REPORT);

    // applySorting 동적 정렬 적용
    QueryDslUtil.applySorting(
        contentQuery,
        pageable,
        Report.class,
        REPORT.getMetadata().getName()
    );

    // countQuery 생성
    JPAQuery<Long> countQuery = queryFactory
        .select(REPORT.count())
        .from(REPORT);

    return QueryDslUtil.fetchPage(contentQuery, countQuery, pageable);
  }

  @Override
  public Optional<ReportInfo> findReportById(UUID reportId){
    // TODO: 반환값 추가 수정 필요
      ReportInfo response = queryFactory
        .select(Projections.constructor(ReportInfo.class,
            REPORT.reportId,
            REPORT.reporter.memberId,
            REPORT.reportedMember.memberId,
            REPORT.reportReason,
            Expressions.stringTemplate("COALESCE({0}, '')", REPORT.description),
            REPORT.reportStatus,
            REPORT.createdDate
        ))
        .from(REPORT)
        .where(REPORT.reportId.eq(reportId))
        .fetchOne();

    return Optional.ofNullable(response);
  }
}
