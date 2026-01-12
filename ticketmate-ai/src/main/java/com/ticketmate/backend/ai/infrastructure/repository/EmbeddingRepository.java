package com.ticketmate.backend.ai.infrastructure.repository;

import com.ticketmate.backend.ai.core.constant.EmbeddingType;
import com.ticketmate.backend.ai.infrastructure.entity.Embedding;
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
   * 주어진 벡터와 가장 유사한 임베딩의 target_id 목록을 반환하는 메서드 (공연)
   * 티켓 오픈 날짜가 지나지 않고 삭제되지 않은 공연만 반환
   *
   * @param vector        비교할 기준 벡터
   * @param limit         반환할 결과의 수
   * @return 유사도 순으로 정렬된 target_id 리스트
   */
  @Query(value = "SELECT target_id FROM embedding e "
    + "WHERE e.embedding_type = 'CONCERT' "
    + "AND (e.embedding_vector <-> CAST(:vector AS vector)) <= 0.90 "
    + "AND EXISTS "
    + "(SELECT 1 FROM ticket_open_date t "
    + "JOIN concert c ON t.concert_concert_id = c.concert_id "
    + "WHERE e.target_id = t.concert_concert_id "
    + "AND t.open_date >= NOW() "
    + "AND c.deleted = false) "
    + "ORDER BY e.embedding_vector <-> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
  List<UUID> findNearestConcertEmbeddings(@Param("vector") float[] vector, @Param("limit") int limit);

  /**
   * 주어진 벡터와 가장 유사한 임베딩의 target_id 목록을 반환하는 메서드 (대리인)
   *
   * @param vector        비교할 기준 벡터
   * @param limit         반환할 결과의 수
   * @return 유사도 순으로 정렬된 target_id 리스트
   */
  @Query(value = "SELECT target_id FROM embedding "
    + "WHERE embedding_type = 'AGENT' "
    + "AND (embedding_vector <-> CAST(:vector AS vector)) <= 0.90 "
    + "ORDER BY embedding_vector <-> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
  List<UUID> findNearestAgentEmbeddings(@Param("vector") float[] vector, @Param("limit") int limit);

}
