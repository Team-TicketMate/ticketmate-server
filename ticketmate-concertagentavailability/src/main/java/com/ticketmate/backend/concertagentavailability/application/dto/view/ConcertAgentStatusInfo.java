package com.ticketmate.backend.concertagentavailability.application.dto.view;

import com.ticketmate.backend.concert.core.constant.RecruitmentStatus;

import java.util.UUID;

public record ConcertAgentStatusInfo (
  UUID concertId, // 공연 PK
  String concertName, // 공연 제목
  String concertThumbnailStoredPath, // 공연 썸네일 URL
  Integer status, // 모집 여부
  Integer matchedClientCount, // 매칭된 의뢰인 수
  Boolean accepting // on/off
) {

}
