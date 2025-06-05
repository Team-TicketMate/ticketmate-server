package com.ticketmate.backend.object.postgres.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.constants.PortfolioType;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Portfolio extends BasePostgresEntity {
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

    public static final int MAX_IMG_COUNT = 20;

    public void addImg(PortfolioImg img) {
        if (this.getPortfolioImgList().size() < MAX_IMG_COUNT) {
            this.getPortfolioImgList().add(img);
            img.setPortfolio(this);
        }
    }
}
