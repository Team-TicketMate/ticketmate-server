package com.ticketmate.backend.domain.search.domain.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AgentSearchResponse implements SearchResult {
  private UUID agentId;

  private String nickname;

  private String profileUrl;

  private String introduction;

  private double averageRating;

  private int reviewCount;

  private double score; // 최종 점수

  @Override
  public void setScore(Double score) {
    this.score = score;
  }

  @Override
  public UUID getId() {
    return this.agentId;
  }
}
