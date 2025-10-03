package com.ticketmate.backend.member.application.mapper;

import com.ticketmate.backend.member.application.dto.response.AgentBankAccountResponse;
import com.ticketmate.backend.member.infrastructure.entity.AgentBankAccount;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentBankAccountMapper {
  // 단건 매핑 규칙
  @Mapping(source = "accountNumberEnc", target = "agentAccountNumber")
  AgentBankAccountResponse toAgentBankAccountResponse(AgentBankAccount entity);

  // AgentBankAccount -> List<AgentBankAccountResponse> (DTO)
  List<AgentBankAccountResponse> toAgentBankAccountResponseList(List<AgentBankAccount> agentBankAccountList);

}
