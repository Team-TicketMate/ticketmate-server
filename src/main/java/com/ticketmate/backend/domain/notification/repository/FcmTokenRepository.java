package com.ticketmate.backend.domain.notification.repository;

import com.ticketmate.backend.domain.notification.domain.entity.FcmToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface FcmTokenRepository extends CrudRepository<FcmToken, String> {
  List<FcmToken> findAllByMemberId(UUID memberId);
}
