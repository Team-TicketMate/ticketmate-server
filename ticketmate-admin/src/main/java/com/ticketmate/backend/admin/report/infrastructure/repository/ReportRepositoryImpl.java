package com.ticketmate.backend.admin.report.infrastructure.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.admin.report.application.dto.response.ReportDetailResponse;
import com.ticketmate.backend.admin.report.application.dto.response.ReportListResponse;
import com.ticketmate.backend.querydsl.infrastructure.util.QueryDslUtil;
import com.ticketmate.backend.report.infrastructure.entity.QReport;
import com.ticketmate.backend.report.infrastructure.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

  private static final QReport REPORT = QReport.report;
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ReportListResponse> filteredReports(Pageable pageable){
    // TODO: 필터링 로직, 반환값 추가 등 수정 필요
    JPAQuery<ReportListResponse> contentQuery = queryFactory
        .select(Projections.constructor(ReportListResponse.class,
            REPORT.reportId,
            REPORT.reporter.memberId,
            REPORT.reportedUser.memberId,
            REPORT.reason,
            REPORT.status,
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
  public Optional<ReportDetailResponse> findReportById(UUID reportId){
    // TODO: 반환값 추가 수정 필요
    ReportDetailResponse response = queryFactory
        .select(Projections.constructor(ReportDetailResponse.class,
            REPORT.reportId,
            REPORT.reporter.memberId,
            REPORT.reportedUser.memberId,
            REPORT.reason,
            REPORT.description,
            REPORT.status,
            REPORT.createdDate
        ))
        .from(REPORT)
        .where(REPORT.reportId.eq(reportId))
        .fetchOne();

    return Optional.ofNullable(response);
  }
}
