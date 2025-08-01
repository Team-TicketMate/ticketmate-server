package com.ticketmate.backend.applicationform.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.QApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.QHopeArea;
import com.ticketmate.backend.concert.infrastructure.entity.QConcertDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplicationFormDetailRepositoryImpl implements ApplicationFormDetailRepositoryCustom{

  private final JPAQueryFactory queryFactory;

  @Override
  public List<ApplicationFormDetail> findAllApplicationFormDetailWithHopeAreaListByApplicationFormId(UUID applicationFormId) {
    QApplicationFormDetail applicationFormDetail = QApplicationFormDetail.applicationFormDetail;
    QHopeArea hopeArea = QHopeArea.hopeArea;
    QConcertDate concertDate = QConcertDate.concertDate;

    return queryFactory
        .select(applicationFormDetail)
        .from(applicationFormDetail)
        .join(applicationFormDetail.concertDate, concertDate).fetchJoin()
        .leftJoin(applicationFormDetail.hopeAreaList, hopeArea).fetchJoin()
        .where(applicationFormDetail.applicationForm.applicationFormId.eq(applicationFormId))
        .orderBy(concertDate.performanceDate.asc(), hopeArea.priority.asc())
        .fetch();
  }
}
