package com.ticketmate.backend.notification.application.mapper;

import com.ticketmate.backend.notification.application.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.notification.infrastructure.entity.FcmToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  // FcmToken -> FcmTokenSaveResponse (엔티티 -> DTO)
  FcmTokenSaveResponse toFcmTokenSaveResponse(FcmToken fcmToken);
}
