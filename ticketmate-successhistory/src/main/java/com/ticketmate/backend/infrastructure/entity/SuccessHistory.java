package com.ticketmate.backend.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.core.constants.SuccessHistoryStatus;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class SuccessHistory extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID successHistoryId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private FulfillmentForm fulfillmentForm;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SuccessHistoryStatus successHistoryStatus;

  public static SuccessHistory create(FulfillmentForm fulfillmentForm) {
    return SuccessHistory.builder()
      .fulfillmentForm(fulfillmentForm)
      .successHistoryStatus(SuccessHistoryStatus.NOT_REVIEWED)
      .build();
  }
}
