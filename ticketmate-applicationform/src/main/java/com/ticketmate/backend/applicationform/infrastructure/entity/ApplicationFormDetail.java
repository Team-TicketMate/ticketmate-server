package com.ticketmate.backend.applicationform.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Slf4j
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApplicationFormDetail extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID applicationFormDetailId;

  @ManyToOne(fetch = FetchType.LAZY)
  private ApplicationForm applicationForm; // 신청서

  @ManyToOne(fetch = FetchType.LAZY)
  private ConcertDate concertDate; // 공연일자

  @Column(nullable = false)
  @Builder.Default
  private Integer requestCount = 1; // 요청 매수

  @Column(columnDefinition = "TEXT")
  private String requirement; // 요청 사항

  @OneToMany(mappedBy = "applicationFormDetail", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<HopeArea> hopeAreaList = new ArrayList<>();

  private static final int HOPE_AREA_MAX_SIZE = 10;

  // 희망구역 설정 메서드
  public void addHopeArea(HopeArea hopeArea) {
    if (hopeAreaList.size() >= HOPE_AREA_MAX_SIZE) {
      throw new CustomException(ErrorCode.HOPE_AREAS_SIZE_EXCEED);
    }
    if (hopeAreaList.stream().anyMatch(area ->
        area.getPriority().equals(hopeArea.getPriority()))) {
      throw new CustomException(ErrorCode.PRIORITY_ALREADY_EXISTS);
    }
    hopeAreaList.add(hopeArea);
    hopeArea.setApplicationFormDetail(this);
  }
}
