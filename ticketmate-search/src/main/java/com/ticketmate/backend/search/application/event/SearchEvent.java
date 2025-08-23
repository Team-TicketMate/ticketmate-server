package com.ticketmate.backend.search.application.event;

import java.util.UUID;

public record SearchEvent(UUID memberId, String keyword) {
}
