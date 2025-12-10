package com.ticketmate.backend.member.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.normalizeAndRemoveSpecialCharacters;
import static com.ticketmate.backend.common.core.util.CommonUtil.nvl;
import static com.ticketmate.backend.member.infrastructure.constant.AgentBankAccountConstants.ACCOUNT_NUMBER_PATTERN;
import static com.ticketmate.backend.member.infrastructure.constant.AgentBankAccountConstants.MAX_ACCOUNT_COUNT;
import static com.ticketmate.backend.member.infrastructure.constant.AgentBankAccountConstants.MAX_ACCOUNT_HOLDER_LENGTH;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.application.dto.request.AgentSaveBankAccountRequest;
import com.ticketmate.backend.member.application.dto.request.AgentUpdateBankAccountRequest;
import com.ticketmate.backend.member.application.dto.response.AgentBankAccountResponse;
import com.ticketmate.backend.member.application.mapper.AgentBankAccountMapper;
import com.ticketmate.backend.member.core.constant.BankCode;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.AgentBankAccount;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.AgentBankAccountRepository;
import com.ticketmate.backend.redis.application.annotation.RedisLock;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentBankAccountService {

  private final AgentBankAccountRepository agentBankAccountRepository;
  private final AgentBankAccountMapper mapper;
  private final MemberService memberService;

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('agent-account', #member.memberId)")
  public void saveAgentBankAccount(Member member, AgentSaveBankAccountRequest request) {
    memberService.validateMemberType(member, MemberType.AGENT);

    long existBankAccountCount = agentBankAccountRepository.countByAgent(member);

    if (existBankAccountCount >= MAX_ACCOUNT_COUNT) {
      log.error("대표계좌는 최대 5개까지만 등록 가능합니다. 현재 존재하는 계좌 수 : {}", existBankAccountCount);
      throw new CustomException(ErrorCode.ACCOUNT_EXCEED);
    }

    // 받아온 계좌번호 정규화
    String accountNumber = normalizeAccountNumber(request.getAccountNumber());

    // 정규화된 계좌번호 길이 검증(한번 더)
    if (!accountNumber.matches("\\d{11,16}")) {
      throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
    }

    boolean primaryAccount = Boolean.TRUE.equals(request.isPrimaryAccount());

    // 첫번째로 등록하는 계좌는 무조건 대표계좌로 설정
    if (!primaryAccount && !agentBankAccountRepository.existsByAgentAndPrimaryAccountTrue(member)) {
      log.debug("첫번째로 등록하는 계좌입니다.");
      primaryAccount = true;
    }

    if (primaryAccount) {
      agentBankAccountRepository.demoteAllPrimaryAccount(member.getMemberId());
    }

    String accountHolder = normalizeAndRemoveSpecialCharacters(request.getAccountHolder());

    AgentBankAccount agentBankAccount = AgentBankAccount
        .create(member, request.getBankCode(), accountHolder, accountNumber, primaryAccount);

    agentBankAccountRepository.save(agentBankAccount);
  }

  @Transactional(readOnly = true)
  public List<AgentBankAccountResponse> getAgentBankAccountList(Member member) {
    memberService.validateMemberType(member, MemberType.AGENT);

    List<AgentBankAccount> agentBankAccountList = agentBankAccountRepository.findAllByAgent(member);

    // 대표계좌 먼저 그 다음 최신 생성일 정렬
    agentBankAccountList.sort(
        Comparator.comparing(AgentBankAccount::isPrimaryAccount).reversed()
            .thenComparing(AgentBankAccount::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder()))
    );

    return mapper.toAgentBankAccountResponseList(agentBankAccountList);
  }

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('agent-account', #member.memberId)")
  public void changePrimaryAccount(UUID agentBankAccountId, Member member) {
    memberService.validateMemberType(member, MemberType.AGENT);

    AgentBankAccount agentBankAccount = findAgentAccountById(agentBankAccountId);

    // 소유권 검증
    validateAgentAccountOwner(agentBankAccount, member);

    // 원하는 계좌를 대표계좌로 변경 전 모든 계좌의 대표계좌 여부 필드상태 false로 세팅(유니크 인덱스)
    agentBankAccountRepository.demoteAllPrimaryAccount(member.getMemberId());

    // 그 후 원하는 계좌 하나만 true로 설정
    int updatedAccountNumber = agentBankAccountRepository
        .setPrimaryAccountExclusively(member.getMemberId(), agentBankAccountId);

    if (updatedAccountNumber <= 0) {
      log.error("대표계좌 변경 중 오류가 발생했습니다.");
      throw new CustomException(ErrorCode.ACCOUNT_UPDATE_FAILED);
    }
  }

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('agent-account', #member.memberId)")
  public void changeAccountInfo(UUID agentBankAccountId, Member member, AgentUpdateBankAccountRequest request) {
    memberService.validateMemberType(member, MemberType.AGENT);

    AgentBankAccount agentBankAccount = findAgentAccountById(agentBankAccountId);

    // 소유권 검증
    validateAgentAccountOwner(agentBankAccount, member);

    BankCode bankCode = (request.getBankCode() != null) ? request.getBankCode() : agentBankAccount.getBankCode();

    String accountHolderOfNvl = nvl(request.getAccountHolder(), "");

    String accountHolder = (!Objects.equals(accountHolderOfNvl, "")) ?
        normalizeAndRemoveSpecialCharacters(accountHolderOfNvl) : agentBankAccount.getAccountHolder();

    String accountNumber = (request.getAccountNumber() != null) ?
        normalizeAccountNumber(request.getAccountNumber()) : agentBankAccount.getAccountNumberEnc();

    if (accountHolder == null || accountHolder.isEmpty() || accountHolder.length() > MAX_ACCOUNT_HOLDER_LENGTH) {
      log.error("예금주 형식이 맞지 않습니다. 요청한 예금주 문자열 : {}", accountHolder);
      throw new CustomException(ErrorCode.INVALID_ACCOUNT_HOLDER);
    }

    if (bankCode == null) {
      log.error("은행정보의 값이 Null 입니다");
      throw new CustomException(ErrorCode.INVALID_BANK_CODE);
    }

    if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
      log.error("계좌번호의 형식이 잘못되었습니다. 요청한 계좌번호 : {}", accountNumber);
      throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
    }

    agentBankAccount.setBankCode(bankCode);
    agentBankAccount.setAccountHolder(accountHolder);
    agentBankAccount.setAccountNumberEnc(accountNumber);
  }

  @Transactional
  @RedisLock(key = "@redisLockKeyManager.generate('agent-account', #member.memberId)")
  public void deleteBankAccount(UUID agentBankAccountId, Member member) {
    memberService.validateMemberType(member, MemberType.AGENT);

    AgentBankAccount agentBankAccount = findAgentAccountById(agentBankAccountId);

    // 소유권 검증
    validateAgentAccountOwner(agentBankAccount, member);

    agentBankAccountRepository.deleteById(agentBankAccountId);

    // 현재 남아있는 계좌 수
    long existBankAccountCount = agentBankAccountRepository.countByAgent(member);

    // 나머지 계좌의 수가 1개만 남았을 시 남아있는 계좌는 대표계좌로 설정
    if (existBankAccountCount == 1L) {
      Optional<AgentBankAccount> optionalBankAccount = agentBankAccountRepository.findFirstByAgentOrderByCreatedDateAsc(member);

      if (optionalBankAccount.isEmpty()) {
        return;
      }

      AgentBankAccount remainingAgentBankAccount = optionalBankAccount.get();

      if (!remainingAgentBankAccount.isPrimaryAccount()) {
        remainingAgentBankAccount.setPrimaryAccount(true);
        agentBankAccountRepository.save(remainingAgentBankAccount);

        log.debug("남은 단일 계좌를 대표로 설정했습니다. bankAccountId={}, memberId={}", remainingAgentBankAccount.getAgentBankAccountId(), member.getMemberId());
      }
    }
  }

  private static String normalizeAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_ACCOUNT_NUMBER);
    }

    String nfkc = Normalizer.normalize(accountNumber, Form.NFKC);
    return nfkc.replaceAll("\\D", "");
  }


  /**
   * 계좌 추출
   */
  public AgentBankAccount findAgentAccountById(UUID agentBankAccountId) {
    return agentBankAccountRepository.findById(agentBankAccountId).orElseThrow(
        () -> {
          log.error("계좌를 찾지 못했습니다. 요청받은 계좌 ID: {}", agentBankAccountId);
          throw new CustomException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
        }
    );
  }

  /**
   * 해당 계좌가 현재 로그인된 사용자의 계좌인지 검증하는 메서드
   */
  public void validateAgentAccountOwner(AgentBankAccount agentBankAccount, Member member) {
    if (!agentBankAccount.getAgent().getMemberId().equals(member.getMemberId())) {
      throw new CustomException(ErrorCode.BANK_ACCOUNT_NOT_OWNED);
    }
  }
}
