package com.ticketmate.backend.service.chat;

import com.ticketmate.backend.object.dto.chat.reqeust.ChatRoomFilteredRequest;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.repository.mongo.ChatRoomRepository;
import com.ticketmate.backend.repository.postgres.application.ApplicationFormRepository;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ApplicationFormRepository applicationFormRepository;

    /**
     * 사용자별 존재하는 채팅방 리스트를 불러오는 메서드입니다.
     */
    @Transactional(readOnly = true)
    public Page<ChatRoomListResponse> getChatRoomList(Member member, ChatRoomFilteredRequest request) {
        // 필터링 관련 필드값 세팅
        Boolean preOpen = null;

        String keyword = (request.getSearchKeyword() != null && request.getSearchKeyword() != "") ?
                request.getSearchKeyword() : "";

        Integer pageNum = (request.getPageNum() != null && request.getPageNum() > 0) ?
                request.getPageNum() : 0;

        if (request.getIsPreOpen().equals(ChatRoomFilteredRequest.PreOpenFilter.PRE_OPEN)) {
            preOpen = true;
        } else if (request.getIsPreOpen().equals(ChatRoomFilteredRequest.PreOpenFilter.NORMAL)) {
            preOpen = false;
        }

        // 채팅방 리스트 불러오기 (20개씩)
        Page<ChatRoom> chatRoomList = chatRoomRepository.search(preOpen, keyword, member, pageNum);

        // 채팅방마다 존재하는 신청폼 Id 리스트에 저장
        List<UUID> applicationFormIdList = chatRoomList.stream()
                .map(ChatRoom::getApplicationFormId).collect(Collectors.toList());

        // 채팅방마다 존재하는 사용자 ID중 상대방 ID만 빼와서 리스트에 저장
        Set<UUID> opponentIdList = chatRoomList.stream()
                .map(room -> opponentIdOf(room, member))
                .collect(Collectors.toSet());

        // 채팅방에 존재하는 신청폼 한번에 조회 (N+1 방지)
        Map<UUID, ApplicationForm> applicationFormMap = applicationFormRepository
                .findAllById(applicationFormIdList)
                .stream()
                .collect(Collectors.toMap(ApplicationForm::getApplicationFormId, Function.identity()));

        // 채팅방에 존재하는 회원 한번에 조회 (N+1 방지)
        Map<UUID, Member> memberMap = memberRepository
                .findAllById(opponentIdList)
                .stream()
                .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

        // TODO 마지막으로 저장된 메시지 일괄조회 로직 or 내부에서 처리 고민

        // 매핑
        List<ChatRoomListResponse> response = chatRoomList.stream()
                .map(room -> chatRoomRepository.toResponse(room, member, applicationFormMap, memberMap))
                        .toList();

        return new PageImpl<>(response, chatRoomList.getPageable(), chatRoomList.getTotalElements());
    }

    /**
     * 자신이 아닌 상대방의 id를 찾아주는 메서드입니다.
     */
    private UUID opponentIdOf(ChatRoom room, Member member) {
        // 여기서 member는 자기 자신입니다.
        return room.getAgentMemberId().equals(member.getMemberId())
                ? room.getClientMemberId()
                : room.getAgentMemberId();
    }
}
