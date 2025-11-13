package com.ticketmate.backend.review.infrastructure.entity;

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
public class ReviewImg extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reviewImgId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Review review;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false, unique = true, length = 512)
  private String storedPath;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private FileExtension fileExtension;

  @Column(nullable = false)
  @PositiveOrZero
  private long sizeBytes;

  public static ReviewImg create(Review review, FileMetadata metadata) {
    return ReviewImg.builder()
        .review(review)
        .originalFilename(metadata.originalFilename())
        .storedPath(metadata.storedPath())
        .fileExtension(metadata.fileExtension())
        .sizeBytes(metadata.sizeBytes())
        .build();
  }
}
