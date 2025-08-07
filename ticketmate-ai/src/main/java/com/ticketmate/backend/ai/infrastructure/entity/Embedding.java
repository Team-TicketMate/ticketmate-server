package com.ticketmate.backend.ai.infrastructure.entity;

import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Embedding extends BasePostgresEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID embeddingId;

  @Column(updatable = false)
  private UUID targetId; // 공연, 대리인 PK (검색어의 경우 null)

  @Column(nullable = false, unique = true)
  private String text; // 단어

  @Column(nullable = false, columnDefinition = "VECTOR(768)")
  @JdbcTypeCode(SqlTypes.VECTOR)
  private float[] embeddingVector; // 임베딩 벡터

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmbeddingType embeddingType; // 임베딩 타입(공연, 대리인, 검색)
}
