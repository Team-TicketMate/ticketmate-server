package com.ticketmate.backend.domain.vertexai.repository;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.domain.vertexai.domain.entity.Embedding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

  Optional<Embedding> findByTextAndEmbeddingType(String text, EmbeddingType embeddingType);

  // 공연 벡터 유사도 검색
  @Query(value = "SELECT target_id FROM embedding WHERE embedding_type = 'CONCERT' ORDER BY embedding_vector <-> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
  List<UUID> findNearestConcerts(@Param("vector") float[] vector, @Param("limit") int limit);

  // 대리인 벡터 유사도 검색
  @Query(value = "SELECT target_id FROM embedding WHERE embedding_type = 'AGENT' ORDER BY embedding_vector <-> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
  List<UUID> findNearestAgents(@Param("vector") float[] vector, @Param("limit") int limit);
}
