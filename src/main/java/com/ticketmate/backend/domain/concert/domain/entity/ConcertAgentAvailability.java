package com.ticketmate.backend.domain.concert.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ticketmate.backend.domain.concert.domain.constant.TicketOpenType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConcertAgentAvailability {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID concertAgentOptionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "concert_id")
  private Concert concert;

  @Enumerated(EnumType.STRING)
  private TicketOpenType ticketOpenType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "agent_id")
  private Member agent;

  @Builder.Default
  private boolean isAccepting = true;
}
