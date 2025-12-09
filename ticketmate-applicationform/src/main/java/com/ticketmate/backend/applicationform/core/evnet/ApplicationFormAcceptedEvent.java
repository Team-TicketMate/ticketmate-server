package com.ticketmate.backend.applicationform.core.evnet;

import java.util.UUID;

public record ApplicationFormAcceptedEvent(
  UUID applicationFormId
) {

}
