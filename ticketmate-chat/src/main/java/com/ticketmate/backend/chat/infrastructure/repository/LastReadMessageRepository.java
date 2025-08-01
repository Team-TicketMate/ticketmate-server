package com.ticketmate.backend.chat.infrastructure.repository;

import com.ticketmate.backend.chat.infrastructure.entity.LastReadMessage;
import org.springframework.data.repository.CrudRepository;

public interface LastReadMessageRepository extends CrudRepository<LastReadMessage, String> {

}
