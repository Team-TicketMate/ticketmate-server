package com.ticketmate.backend.repository.redis;

import com.ticketmate.backend.object.redis.LastReadMessage;
import org.springframework.data.repository.CrudRepository;

public interface LastReadMessageRepository extends CrudRepository<LastReadMessage, String> {

}
