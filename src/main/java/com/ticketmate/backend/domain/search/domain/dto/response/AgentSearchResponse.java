package com.ticketmate.backend.domain.search.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private Double score; // 최종 점수

  // QueryDSL 프로젝션을 위한 생성자
  public AgentSearchResponse(UUID agentId, String nickname, String profileUrl, String introduction, double averageRating, int reviewCount){
    this.agentId = agentId;
    this.nickname = nickname;
    this.profileUrl = profileUrl;
    this.introduction = introduction;
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
  }

  @Override
  public void setScore(Double score) {
    this.score = score;
  }

  @Override
  @JsonIgnore
  public UUID getId() {
    return this.agentId;
  }
}
