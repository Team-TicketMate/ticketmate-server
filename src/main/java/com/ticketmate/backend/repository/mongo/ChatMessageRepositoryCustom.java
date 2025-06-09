package com.ticketmate.backend.repository.mongo;

import java.util.UUID;

public interface ChatMessageRepositoryCustom {
    long markReadUpTo(String roomId, UUID readerId);
}
