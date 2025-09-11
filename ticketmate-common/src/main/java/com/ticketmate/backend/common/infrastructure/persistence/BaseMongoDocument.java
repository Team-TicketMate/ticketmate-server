package com.ticketmate.backend.common.infrastructure.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseMongoDocument {

  @CreatedDate
  private Instant createdDate;

  @LastModifiedDate
  private Instant updatedDate;
}
