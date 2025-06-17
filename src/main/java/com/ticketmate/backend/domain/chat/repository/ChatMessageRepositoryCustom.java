package com.ticketmate.backend.domain.chat.repository;

import java.util.UUID;

public interface ChatMessageRepositoryCustom {

  long markReadUpTo(String chatRoomId, UUID readerId);
}
