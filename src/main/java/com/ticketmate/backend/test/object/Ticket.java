package com.ticketmate.backend.test.object;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID ticketId;

  private Long quantity; // 티켓 수량

  public Ticket(Long quantity) {
    this.quantity = quantity;
  }

  public void decrease(Long amount) {
    if (this.quantity - amount < 0) {
      throw new IllegalArgumentException("티켓 수량이 부족합니다.");
    }
    this.quantity -= amount;
  }
}

