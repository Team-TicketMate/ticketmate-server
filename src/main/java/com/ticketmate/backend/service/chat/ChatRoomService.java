package com.ticketmate.backend.service.chat;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.dto.chat.request.ChatRoomFilteredRequest;
import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.repository.mongo.ChatMessageRepository;
import com.ticketmate.backend.repository.mongo.ChatRoomRepository;
import com.ticketmate.backend.repository.postgres.application.ApplicationFormRepository;
import com.ticketmate.backend.repository.postgres.member.MemberRepository;
import com.ticketmate.backend.util.common.MongoMapper;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ticketmate.backend.util.common.CommonUtil.opponentIdOf;
import static com.ticketmate.backend.util.exception.ErrorCode.CHAT_ROOM_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MongoMapper mongoMapper;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 사용자별 존재하는 채팅방 리스트를 불러오는 메서드입니다.
     */
    @Transactional(readOnly = true)
    public Page<ChatRoomListResponse> getChatRoomList(Member member, ChatRoomFilteredRequest request) {
        // 필터링 관련 필드값 세팅
        TicketOpenType preOpen = null;

        String keyword = (request.getSearchKeyword() != null && request.getSearchKeyword() != "") ?
                request.getSearchKeyword() : "";

        Integer pageNumber = (request.getPageNumber() != null && request.getPageNumber() > 0) ?
                request.getPageNumber() : 0;

        if (request.getIsPreOpen().equals(ChatRoomFilteredRequest.PreOpenFilter.PRE_OPEN)) {
            // 선예매만 필터링
            preOpen = TicketOpenType.PRE_OPEN;
        } else if (request.getIsPreOpen().equals(ChatRoomFilteredRequest.PreOpenFilter.NORMAL)) {
            // 일반예매만 필터링
            preOpen = TicketOpenType.GENERAL_OPEN;
        }

        // 채팅방 리스트 불러오기 (20개씩)
        Page<ChatRoom> chatRoomList = chatRoomRepository.search(preOpen, keyword, member, pageNumber);

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

        List<Object> countList = redisTemplate.executePipelined((RedisCallback<Object>) con -> {
            chatRoomList.forEach(r -> {
                String redisKey = ("unRead:%s:%s").formatted(r.getRoomId(), member.getMemberId());
                con.stringCommands().get(redisKey.getBytes());
            });
            return null;
        });



        AtomicInteger i = new AtomicInteger();
        List<ChatRoomListResponse> response = chatRoomList.stream()
                .map(r -> {
                    int unReadMessageCount = parseInt(countList.get(i.getAndIncrement()));
                    return chatRoomRepository.toResponse(
                            r, member, applicationFormMap, memberMap, unReadMessageCount);
                })
                .toList();

        return new PageImpl<>(response, chatRoomList.getPageable(), chatRoomList.getTotalElements());
    }

    /**
     * 현재 진행한 채팅 메시지 불러오는 메서드
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatMessage(Member member, String chatRoomId){
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CHAT_ROOM_NOT_FOUND));
        validateRoomMember(room, member);

        List<ChatMessage> massageList = chatMessageRepository
                .findByRoomIdOrderBySendDateAsc(chatRoomId);

        return massageList.stream().map(mongoMapper::toResponse).toList();
    }

    /**
     * 방 참가자 검증
     */
    private void validateRoomMember(ChatRoom room, Member member) {
        UUID id = member.getMemberId();
        if (!id.equals(room.getAgentMemberId()) && !id.equals(room.getClientMemberId())) {
            throw new CustomException(ErrorCode.NO_AUTH_TO_ROOM);
        }
    }

    /**
     * 형변환 메서드
     */
    private int parseInt(Object object){
        if(object == null) return 0;
        if(object instanceof byte[] b) return Integer.parseInt(new String(b));
        return Integer.parseInt(object.toString());
    }
}
