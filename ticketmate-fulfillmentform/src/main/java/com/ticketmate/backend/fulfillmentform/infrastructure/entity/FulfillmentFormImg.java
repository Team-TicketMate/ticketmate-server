package com.ticketmate.backend.fulfillmentform.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.storage.core.constant.FileExtension;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PositiveOrZero;
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
public class FulfillmentFormImg extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID fulfillmentFormImgId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private FulfillmentForm fulfillmentForm;

  @Column(nullable = false)
  private String originalFilename; // 원본 파일명

  @Column(nullable = false, unique = true, length = 512)
  private String storedPath; // 파일 저장 경로

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FileExtension fileExtension; // 파일 확장자

  @Column(nullable = false)
  @PositiveOrZero
  private long sizeBytes;

  public static FulfillmentFormImg of(FulfillmentForm fulfillmentForm, FileMetadata metadata) {
    return FulfillmentFormImg.builder()
      .fulfillmentForm(fulfillmentForm)
      .originalFilename(metadata.originalFilename())
      .storedPath(metadata.storedPath())
      .fileExtension(metadata.fileExtension())
      .sizeBytes(metadata.sizeBytes())
      .build();
  }
}
