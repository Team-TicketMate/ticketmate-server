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

  void deleteByText(String text);

  /**
   * 주어진 벡터와 가장 유사한 임베딩의 target_id 목록을 반환하는 메서드
   * @param vector 비교할 기준 벡터
   * @param limit 반환할 결과의 수
   * @param embeddingType 검색할 임베딩의 타입 (CONCERT, AGENT 등)
   * @return 유사도 순으로 정렬된 target_id 리스트
   */
  @Query(value = "SELECT target_id FROM embedding "
                 + "WHERE embedding_type = :#{#embeddingType.name()} "
                 + "ORDER BY embedding_vector <-> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
  List<UUID> findNearestEmbeddings(@Param("vector") float[] vector, @Param("limit") int limit, @Param("embeddingType") EmbeddingType embeddingType);
}
