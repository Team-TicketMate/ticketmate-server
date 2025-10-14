package com.ticketmate.backend.review.infrastructure.entity;

import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Review extends BasePostgresEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID reviewId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, unique = true)
  private ApplicationForm applicationForm;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member client;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Member agent;

  @Column(nullable = false)
  private float rating;

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
    if (CommonUtil.nullOrEmpty(reviewImgs)) return;
    for (ReviewImg img : reviewImgs) {
      if (img.getReview() != this) {
        img.setReview(this);
      }
    }
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
    this.agentCommentedDate = TimeUtil.now();
  }
}
