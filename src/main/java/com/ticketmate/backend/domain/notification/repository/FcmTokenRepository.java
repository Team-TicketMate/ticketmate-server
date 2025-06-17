package com.ticketmate.backend.domain.notification.repository;

import com.ticketmate.backend.domain.notification.domain.entity.FcmToken;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface FcmTokenRepository extends CrudRepository<FcmToken, String> {

  List<FcmToken> findAllByMemberId(UUID memberId);
}
