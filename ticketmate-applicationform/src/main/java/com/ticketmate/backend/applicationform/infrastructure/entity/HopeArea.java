package com.ticketmate.backend.applicationform.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class HopeArea extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID hopeAreaId;

  @ManyToOne(fetch = FetchType.LAZY)
  private ApplicationFormDetail applicationFormDetail;

  @Column(nullable = false)
  private Integer priority; // 순위 (1~10)

  @Column(nullable = false)
  private String location; // 위치 (예: A구역, B구역)

  @Column(nullable = false)
  private Long price; // 가격
}
