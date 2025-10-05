package com.ticketmate.backend.review.infrastructure.entity;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Review extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reviewId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, unique = true)
  private ApplicationForm applicationForm;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Member client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Member agent;

  @Column(nullable = false)
  private Float rating;

  @Column(nullable = false, length = 300)
  private String comment;

  @Column(length = 300)
  private String agentComment;

  @Column(columnDefinition = "TIMESTAMPTZ(0)")
  private Instant agentCommentedDate;

  @Builder.Default
  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ReviewImg> reviewImgList = new ArrayList<>();

  public static Review create(ApplicationForm applicationForm, Member client, Member agent, Float rating, String comment) {
    return Review.builder()
        .applicationForm(applicationForm)
        .client(client)
        .agent(agent)
        .rating(rating)
        .comment(comment)
        .build();
  }

  public void addReviewImgs(List<ReviewImg> reviewImgs) {
    this.reviewImgList.addAll(reviewImgs);
  }

  public void update(Float rating, String comment) {
    if (rating != null) this.rating = rating;
    if (comment != null) this.comment = comment;
  }

  public void removeReviewImg(ReviewImg reviewImg) {
    this.reviewImgList.remove(reviewImg);
  }

  public void addAgentComment(String comment) {
    this.agentComment = comment;
    this.agentCommentedDate = Instant.now();
  }
}
