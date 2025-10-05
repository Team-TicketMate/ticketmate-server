package com.ticketmate.backend.review.infrastructure.entity;

import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.storage.core.constant.FileExtension;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
