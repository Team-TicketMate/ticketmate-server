package com.ticketmate.backend.infrastructure.repository;

import com.ticketmate.backend.concert.core.constant.ConcertType;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SuccessHistoryRow {

  UUID getFulfillmentId();

  String getConcertName();

  String getConcertThumbnailStoredPath();

  ConcertType getConcertType();

  LocalDateTime getCreateDate();

  String getClientNickname();

  UUID getReviewId(); // 리뷰 없으면 null

  Float getReviewRating(); // 리뷰 없으면 null
}
