package com.ticketmate.backend.applicationform.infrastructure.entity;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;
import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.REQUIREMENT_MAX_LENGTH;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.concert.infrastructure.entity.ConcertDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationFormDetail extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, unique = true)
  private UUID applicationFormDetailId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private ApplicationForm applicationForm; // 신청서

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private ConcertDate concertDate; // 공연일자

  @PositiveOrZero
  @Column(nullable = false)
  private int requestCount; // 요청 매수

  @Column(length = REQUIREMENT_MAX_LENGTH)
  private String requirement; // 요청 사항 // TODO: 추후 VO 로 분리

  @OneToMany(mappedBy = "applicationFormDetail", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<HopeArea> hopeAreaList = new ArrayList<>();

  public static ApplicationFormDetail create(ConcertDate concertDate, int requestCount, String requirement) {
    return ApplicationFormDetail.builder()
        .concertDate(concertDate)
        .requestCount(requestCount)
        .requirement(requirement)
        .hopeAreaList(new ArrayList<>())
        .build();
  }

  // 희망구역 설정 메서드
  public void addHopeArea(HopeArea hopeArea) {
    if (hopeAreaList.size() >= HOPE_AREA_MAX_SIZE) {
      throw new CustomException(ErrorCode.HOPE_AREAS_SIZE_EXCEED, HOPE_AREA_MAX_SIZE);
    }
    if (hopeAreaList.stream().anyMatch(area ->
        area.getPriority() == hopeArea.getPriority())) {
      throw new CustomException(ErrorCode.PRIORITY_ALREADY_EXISTS);
    }
    hopeAreaList.add(hopeArea);
    hopeArea.setApplicationFormDetail(this);
  }
}
