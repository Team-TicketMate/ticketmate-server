package com.ticketmate.backend.global.mapper;

import com.ticketmate.backend.domain.admin.dto.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.domain.admin.dto.response.PortfolioForAdminResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.request.HopeAreaRequest;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormDetailResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.HopeAreaResponse;
import com.ticketmate.backend.domain.applicationform.domain.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationForm;
import com.ticketmate.backend.domain.applicationform.domain.entity.ApplicationFormDetail;
import com.ticketmate.backend.domain.applicationform.domain.entity.HopeArea;
import com.ticketmate.backend.domain.applicationform.domain.entity.RejectionReason;
import com.ticketmate.backend.domain.chat.domain.dto.response.ChatMessageResponse;
import com.ticketmate.backend.domain.chat.domain.entity.ChatMessage;
import com.ticketmate.backend.domain.concert.domain.dto.response.ConcertDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.dto.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.domain.concert.domain.entity.ConcertDate;
import com.ticketmate.backend.domain.concert.domain.entity.TicketOpenDate;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.domain.concerthall.domain.dto.response.ConcertHallInfoResponse;
import com.ticketmate.backend.domain.concerthall.domain.entity.ConcertHall;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberInfoResponse;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.notification.domain.dto.response.FcmTokenSaveResponse;
import com.ticketmate.backend.domain.notification.domain.entity.FcmToken;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.domain.entity.PortfolioImg;
import com.ticketmate.backend.global.util.common.CommonUtil;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface EntityMapper {

  /*
  ======================================공연장======================================
   */

  // ConcertHall -> ConcertHallFilteredResponse (엔티티 -> DTO)
  ConcertHallFilteredResponse toConcertHallFilteredResponse(ConcertHall concertHall);

  // ConcertHall -> ConcertHallInfoResponse (엔티티 -> DTO)
  ConcertHallInfoResponse toConcertHallInfoResponse(ConcertHall concertHall);

  /*
  ======================================공연======================================
   */

  // Concert -> ConcertFilteredResponse (엔티티 -> DTO)

  // List<ConcertDate> -> List<ConcertDateInfoResponse> (엔티티 리스트 -> DTO 리스트)
  List<ConcertDateInfoResponse> toConcertDateInfoResponseList(List<ConcertDate> concertDateList);

  // List<TicketOpenDate> -> List<TicketOpenDateInfoResponse> (엔티티 리스트 -> DTO 리스트)
  List<TicketOpenDateInfoResponse> toTicketOpenDateInfoResponseList(List<TicketOpenDate> ticketOpenDateList);


  /*
  ======================================포트폴리오======================================
   */

  // Portfolio -> PortfolioFilteredAdminResponse (엔티티 -> DTO)
  @Mapping(source = "member.memberId", target = "memberId")
  @Mapping(source = "member.username", target = "username")
  @Mapping(source = "member.nickname", target = "nickname")
  @Mapping(source = "member.name", target = "name")
  PortfolioFilteredAdminResponse toPortfolioFilteredAdminResponse(Portfolio portfolio);

  // Portfolio -> PortfolioForAdminResponse (엔티티 -> DTO)
  @Mapping(source = "member.memberId", target = "memberId")
  @Mapping(source = "member.nickname", target = "nickname")
  @Mapping(source = "member.phone", target = "phone")
  @Mapping(source = "member.profileUrl", target = "profileUrl")
  @Mapping(source = "member.memberType", target = "memberType")
  @Mapping(target = "portfolioImgList", expression = "java(mapToFilePathList(portfolio.getPortfolioImgList()))")
  PortfolioForAdminResponse toPortfolioForAdminResponse(Portfolio portfolio);

  // PortfolioImg 엔티티 리스트에서 각 filePath만 추출하여 String 리스트로 변환
  @Named("mapToFilePathList")
  default List<String> mapToFilePathList(List<PortfolioImg> imgList) {
    if (CommonUtil.nullOrEmpty(imgList)) {
      return List.of();
    }
    return imgList.stream()
        .map(PortfolioImg::getFilePath)
        .collect(Collectors.toList());
  }
  

  /*
  ======================================신청서======================================
   */

  // List<HopeAreaRequest> -> List<HopeArea> (DTO 리스트 -> 엔티티 리스트)
  List<HopeArea> toHopeAreaList(List<HopeAreaRequest> hopeAreaRequestList);

  // HopeArea -> HopeAreaResponse (엔티티 -> DTO)
  HopeAreaResponse toHopeAreaResponse(HopeArea hopeArea);

  // List<HopeArea> -> List<HopeAreaResponse> (엔티티 리스트 -> DTO 리스트)
  List<HopeAreaResponse> toHopeAreaResponseList(List<HopeArea> hopeAreaList);

  // ApplicationFormDetail -> ApplicationFormDetailResponse (엔티티 -> DTO)
  @Mapping(source = "concertDate.performanceDate", target = "performanceDate")
  @Mapping(source = "concertDate.session", target = "session")
  @Mapping(source = "hopeAreaList", target = "hopeAreaResponseList")
  ApplicationFormDetailResponse toApplicationFormDetailResponse(ApplicationFormDetail applicationFormDetail);

  // List<ApplicationFormDetail> -> List<ApplicationFormDetailResponse> (엔티티 리스트 -> DTO 리스트)
  List<ApplicationFormDetailResponse> toApplicationFormDetailResponseList(List<ApplicationFormDetail> applicationFormDetailList);

  // ApplicationForm -> ApplicationFormFilteredResponse (엔티티 -> DTO)
  @Mapping(source = "concert.concertName", target = "concertName")
  @Mapping(source = "concert.concertThumbnailUrl", target = "concertThumbnailUrl")
  @Mapping(source = "agent.nickname", target = "agentNickname")
  @Mapping(source = "client.nickname", target = "clientNickname")
  @Mapping(source = "updatedDate", target = "submittedDate")
  ApplicationFormFilteredResponse toApplicationFormFilteredResponse(ApplicationForm applicationForm);

  /*
  ======================================거절 사유======================================
   */
  // RejectionReason -> RejectionReasonResponse (엔티티 -> DTO)
  RejectionReasonResponse toRejectionReasonResponse(RejectionReason rejectionReason);
  
  
  /*
  ======================================FCM======================================
   */

  // FcmToken -> FcmTokenSaveResponse
  FcmTokenSaveResponse toFcmTokenSaveResponse(FcmToken fcmToken);


  /*
  ======================================채팅 (Mongo)======================================
   */

  /**
   * ChatMessage(Mongo) → ChatMessageResponse(DTO)
   */
  @Mapping(source = "message.chatMessageId", target = "messageId")
  @Mapping(source = "message.senderNickName", target = "senderNickname")
  @Mapping(source = "message.senderProfileUrl", target = "profileUrl")
  @Mapping(target = "mine", expression = "java(message.getSenderId().equals(currentMemberId))")
  ChatMessageResponse toChatMessageResponse(ChatMessage message, UUID currentMemberId);


  /*
  ======================================사용자======================================
   */

  // Member -> MemberInfoResponse (DTO)
  MemberInfoResponse toMemberInfoResponse(Member member);
}
