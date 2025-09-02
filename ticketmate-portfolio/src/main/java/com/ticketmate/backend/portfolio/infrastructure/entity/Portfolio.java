package com.ticketmate.backend.portfolio.infrastructure.entity;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.infrastructure.persistence.BasePostgresEntity;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.portfolio.core.constant.PortfolioStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class Portfolio extends BasePostgresEntity {

  public static final int MAX_IMG_COUNT = 20;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID portfolioId;
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false, unique = true)
  private Member member;
  @Column(nullable = false)
  private String portfolioDescription;  // 자기소개
  @Builder.Default
  @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PortfolioImg> portfolioImgList = new ArrayList<>();
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PortfolioStatus portfolioStatus;

  public static Portfolio create(Member member, String portfolioDescription, PortfolioStatus portfolioStatus) {
    return Portfolio.builder()
        .member(member)
        .portfolioDescription(portfolioDescription)
        .portfolioImgList(new ArrayList<>())
        .portfolioStatus(portfolioStatus)
        .build();
  }

  public void addPortfolioImg(PortfolioImg img) {
    if (this.getPortfolioImgList().size() >= MAX_IMG_COUNT) {
      throw new CustomException(ErrorCode.PORTFOLIO_IMG_COUNT_EXCEED);
    }
    this.getPortfolioImgList().add(img);
    img.setPortfolio(this);
  }

  // PortfolioStatus 상태 변경
  public void transitionPortfolioStatus(PortfolioStatus portfolioStatus) {
    if (!canTransitionPortfolioStatus(portfolioStatus)) {
      throw new CustomException(ErrorCode.PORTFOLIO_STATUS_TRANSITION_ERROR);
    }
    this.portfolioStatus = portfolioStatus;
  }

  // 현재 PortfolioStatus 에서 요청된 상태로 변경 가능 여부
  private boolean canTransitionPortfolioStatus(PortfolioStatus portfolioStatus) {
    if (portfolioStatus == null || this.portfolioStatus.equals(portfolioStatus)) {
      return false;
    }
    return switch (this.portfolioStatus) {
      case PENDING_REVIEW -> portfolioStatus == PortfolioStatus.REVIEWING;
      case REVIEWING -> portfolioStatus == PortfolioStatus.APPROVED || portfolioStatus == PortfolioStatus.REJECTED;
      case APPROVED, REJECTED -> false;
    };
  }
}
