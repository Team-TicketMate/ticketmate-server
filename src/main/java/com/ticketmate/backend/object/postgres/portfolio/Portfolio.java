package com.ticketmate.backend.object.postgres.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Portfolio extends BasePostgresEntity {

  public static final int MAX_IMG_COUNT = 20;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID portfolioId;
  @OneToOne(fetch = FetchType.LAZY)
  private Member member;
  @Column(columnDefinition = "TEXT")
  private String portfolioDescription;  // 자기소개
  @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PortfolioImg> portfolioImgList = new ArrayList<>();
  @Enumerated(EnumType.STRING)
  private PortfolioType portfolioType;

  public void addImg(PortfolioImg img) {
    if (this.getPortfolioImgList().size() < MAX_IMG_COUNT) {
      this.getPortfolioImgList().add(img);
      img.setPortfolio(this);
    }
  }
}
