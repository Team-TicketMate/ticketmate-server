package com.ticketmate.backend.object.postgres.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PortfolioImg extends BasePostgresEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID portfolioImgId;

    @Column(columnDefinition = "TEXT", length = 1024)
    private String imgName;  // 포트폴리오 이미지 이름

    @ManyToOne(fetch = FetchType.LAZY)
    private Portfolio portfolio;
}
