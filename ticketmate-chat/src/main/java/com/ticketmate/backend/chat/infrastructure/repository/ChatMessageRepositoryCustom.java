package com.ticketmate.backend.chat.infrastructure.repository;

import java.util.UUID;

public interface ChatMessageRepositoryCustom {

  long markReadUpTo(String chatRoomId, UUID readerId);
}
