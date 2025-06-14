package com.ticketmate.backend.repository.mongo;

import com.ticketmate.backend.object.constants.TicketOpenType;
import com.ticketmate.backend.object.dto.chat.response.ChatRoomListResponse;
import com.ticketmate.backend.object.mongo.chat.ChatRoom;
import com.ticketmate.backend.object.postgres.Member.Member;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ticketmate.backend.util.common.CommonUtil.opponentIdOf;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {
    private static final int PAGE_SIZE = 20;
    private final MongoTemplate mongoTemplate;

    /**
     * MongoDB 동적 쿼리 생성
     */
    @Override
    public Page<ChatRoom> search(TicketOpenType preOpen, String keyword, Member member, Integer pageNum) {
        log.debug("받아온 키워드 : {}", keyword);
        UUID memberId = member.getMemberId();

        log.debug("선예매 일반예매 구분 : {}", preOpen);

        // 1. 내가 ‘대리인’으로 들어가 있는 방 조건
        Criteria asAgent = Criteria.where("agentMemberId").is(memberId);
        if (preOpen != null) {
            asAgent = asAgent.and("preOpen").is(preOpen);
        }
        if (!keyword.isEmpty()) {
            asAgent = asAgent.and("clientMemberNickname").regex(keyword, "i");
        }

        // 2. 내가 ‘의뢰인’으로 들어가 있는 방 조건
        Criteria asClient = Criteria.where("clientMemberId").is(memberId);
        if (preOpen != null) {
            asClient = asClient.and("preOpen").is(preOpen);
        }
        if (!keyword.isEmpty())         {
            asClient = asClient.and("agentMemberNickname").regex(keyword, "i"); }

        // 3. 두 분기 OR로 결합
        Criteria criteria = new Criteria().orOperator(asAgent, asClient);

        // 마지막 채팅 메시지 시간 기준 내림차순 정렬 (6개씩)
        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "lastMessageTime"))
                .skip((long) pageNum * PAGE_SIZE)
                .limit(PAGE_SIZE);

        List<ChatRoom> chatRoomList = mongoTemplate.find(query, ChatRoom.class);
        log.debug("조회된 채팅방 객체 개수 : {}", chatRoomList.size());

        long totalCount = mongoTemplate.count(Query.query(criteria), ChatRoom.class);
        log.debug("Count로 조회된 채팅방 개수 : {}", totalCount);

        return new PageImpl<>(chatRoomList,
                PageRequest.of(pageNum, PAGE_SIZE),
                totalCount);
    }

    /**
     * 내부 매퍼
     */
    public ChatRoomListResponse toResponse(ChatRoom room, Member member,
                                           Map<UUID, ApplicationForm> applicationFormMap,
                                           Map<UUID, Member> memberMap, int unRead) {

        // 매핑을 위한 값 세팅
        UUID opponentId = opponentIdOf(room, member);
        Member other    = memberMap.get(opponentId);
        ApplicationForm applicationForm = applicationFormMap.get(room.getApplicationFormId());

        return ChatRoomListResponse.builder()
                .unRead(unRead)
                .chatRoomId(room.getRoomId())
                .chatRoomName(other.getNickname())  // 상대방 닉네임 출력
                .ticketOpenType(room.getPreOpen())
                .lastChatMessage(room.getLastMessage())
                .concertThumbnailUrl(applicationForm.getConcert().getConcertThumbnailUrl())
                .lastChatSendTime(room.getLastMessageTime())
                .profileUrl(other.getProfileUrl())
                .build();
    }
}
