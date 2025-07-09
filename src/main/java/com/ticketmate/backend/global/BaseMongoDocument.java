package com.ticketmate.backend.global;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseMongoDocument {

  @CreatedDate
  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime createdDate;

  @LastModifiedDate
  @Column(nullable = false, columnDefinition = "TIMESTAMP(0)")
  private LocalDateTime updatedDate;
}
