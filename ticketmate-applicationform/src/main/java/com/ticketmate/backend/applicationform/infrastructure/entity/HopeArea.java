package com.ticketmate.backend.applicationform.infrastructure.entity;

import static com.ticketmate.backend.applicationform.infrastructure.constant.ApplicationFormConstants.HOPE_AREA_MAX_SIZE;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
public class HopeArea extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, unique = true)
  private UUID hopeAreaId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private ApplicationFormDetail applicationFormDetail;

  @Min(1)
  @Max(HOPE_AREA_MAX_SIZE)
  @Column(nullable = false)
  private int priority; // 순위 (1~10)

  @Column(nullable = false)
  private String location; // 위치 (예: A구역, B구역)

  @Positive
  @Column(nullable = false)
  private int price; // 가격

  public static HopeArea create(ApplicationFormDetail applicationFormDetail, int priority, String location, int price) {
    return HopeArea.builder()
        .applicationFormDetail(applicationFormDetail)
        .priority(priority)
        .location(location)
        .price(price)
        .build();
  }
}
