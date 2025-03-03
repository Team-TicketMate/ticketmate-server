package com.ticketmate.backend.repository.redis;

import com.ticketmate.backend.object.redis.FcmToken;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface FcmTokenRepository extends CrudRepository<FcmToken, UUID> {
}
