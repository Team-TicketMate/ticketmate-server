package com.ticketmate.backend.member.infrastructure.constant;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AgentBankAccountConstants {

  public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{11,16}$");
}
