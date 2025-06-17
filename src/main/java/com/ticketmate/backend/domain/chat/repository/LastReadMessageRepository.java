package com.ticketmate.backend.domain.chat.repository;

import com.ticketmate.backend.domain.chat.domain.entity.LastReadMessage;
import org.springframework.data.repository.CrudRepository;

public interface LastReadMessageRepository extends CrudRepository<LastReadMessage, String> {

}
