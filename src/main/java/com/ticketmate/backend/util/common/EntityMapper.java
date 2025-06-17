package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.admin.response.ConcertHallFilteredAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioFilteredAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.application.request.HopeAreaRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormDetailResponse;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.object.dto.application.response.HopeAreaResponse;
import com.ticketmate.backend.object.dto.chat.response.ChatMessageResponse;
import com.ticketmate.backend.object.dto.concert.response.ConcertDateInfoResponse;
import com.ticketmate.backend.object.dto.concert.response.TicketOpenDateInfoResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallInfoResponse;
import com.ticketmate.backend.object.dto.fcm.response.FcmTokenSaveResponse;
import com.ticketmate.backend.object.mongo.chat.ChatMessage;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.ApplicationFormDetail;
import com.ticketmate.backend.object.postgres.application.HopeArea;
import com.ticketmate.backend.object.postgres.concert.ConcertDate;
import com.ticketmate.backend.object.postgres.concert.TicketOpenDate;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.postgres.portfolio.PortfolioImg;
import com.ticketmate.backend.object.redis.FcmToken;
import java.util.List;
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

  // ConcertHall -> ConcertHallFilteredAdminResponse (엔티티 -> DTO)
  ConcertHallFilteredAdminResponse toConcertHallFilteredAdminResponse(ConcertHall concertHall);

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
  @Mapping(source = "client.memberId", target = "clientId")
  @Mapping(source = "agent.memberId", target = "agentId")
  @Mapping(source = "concert.concertId", target = "concertId")
  @Mapping(source = "ticketOpenDate.openDate", target = "openDate")
  @Mapping(source = "applicationFormDetailList", target = "applicationFormDetailResponseList")
  ApplicationFormFilteredResponse toApplicationFormFilteredResponse(ApplicationForm applicationForm);
  
  
    /*
    ======================================FCM======================================
     */

  // FcmToken -> FcmTokenSaveResponse
  FcmTokenSaveResponse toFcmTokenSaveResponse(FcmToken fcmToken);


     /*
    ======================================채팅 (Mongo)======================================
     */

  // ChatMessage(Mongo) → ChatMessageResponse(DTO)
  @Mapping(source = "chatMessageId", target = "messageId")
  @Mapping(source = "senderNickName", target = "senderNickname")
  @Mapping(source = "senderProfileUrl", target = "profileUrl")
  ChatMessageResponse toChatMessageResponse(ChatMessage message);
}
