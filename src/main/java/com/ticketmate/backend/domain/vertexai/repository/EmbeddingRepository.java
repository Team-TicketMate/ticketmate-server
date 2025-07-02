package com.ticketmate.backend.domain.vertexai.repository;

import com.ticketmate.backend.domain.vertexai.domain.constant.EmbeddingType;
import com.ticketmate.backend.domain.vertexai.domain.entity.Embedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingRepository extends JpaRepository<Embedding, UUID> {

  Optional<Embedding> findByTextAndEmbeddingType(String text, EmbeddingType embeddingType);

}
