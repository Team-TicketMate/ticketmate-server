package com.ticketmate.backend.common.infrastructure.persistence;

import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@ToString
@SuperBuilder
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BasePostgresEntity {

  // 생성일
  @CreatedDate
  @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ(0)")
  private Instant createdDate;

  // 수정일
  @LastModifiedDate
  @Column(nullable = false, columnDefinition = "TIMESTAMPTZ(0)")
  private Instant updatedDate;

  // 삭제 여부
  @Column(nullable = false)
  private boolean deleted = false;

  // 삭제 일시
  @Column(columnDefinition = "TIMESTAMPTZ(0)")
  private Instant deletedDate;

  public void delete() {
    this.deleted = true;
    this.deletedDate = TimeUtil.now();
  }
}
