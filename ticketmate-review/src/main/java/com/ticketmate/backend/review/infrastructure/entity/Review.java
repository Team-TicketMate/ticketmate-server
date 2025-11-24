package com.ticketmate.backend.review.infrastructure.entity;

import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
  private FulfillmentForm fulfillmentForm;

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

  public static Review create(FulfillmentForm fulfillmentForm, Member client, Member agent, float rating, String comment) {
    return Review.builder()
        .fulfillmentForm(fulfillmentForm)
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
