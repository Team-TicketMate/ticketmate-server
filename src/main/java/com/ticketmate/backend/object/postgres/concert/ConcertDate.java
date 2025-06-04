package com.ticketmate.backend.object.postgres.concert;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.object.postgres.global.BasePostgresEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConcertDate extends BasePostgresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID concertDateId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Concert concert; // 공연

    @Column(nullable = false, columnDefinition = "TIMESTAMP(0)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime performanceDate; // 공연 일자

    @Column(nullable = false)
    @Builder.Default
    private Integer session = 1; // 회차
}
