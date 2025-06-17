package com.ticketmate.backend.repository.redis;

import com.ticketmate.backend.object.redis.FcmToken;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface FcmTokenRepository extends CrudRepository<FcmToken, String> {

  List<FcmToken> findAllByMemberId(UUID memberId);
}
