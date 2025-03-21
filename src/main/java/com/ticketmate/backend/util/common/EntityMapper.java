package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.application.request.HopeAreaRequest;
import com.ticketmate.backend.object.dto.application.response.ApplicationFormInfoResponse;
import com.ticketmate.backend.object.dto.application.response.HopeAreaResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallInfoResponse;
import com.ticketmate.backend.object.dto.fcm.response.FcmTokenSaveResponse;
import com.ticketmate.backend.object.postgres.application.ApplicationForm;
import com.ticketmate.backend.object.postgres.application.HopeArea;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.redis.FcmToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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



    /*
    ======================================포트폴리오======================================
     */

    // Portfolio -> PortfolioListForAdminResponse (엔티티 -> DTO)
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.nickname", target = "nickname")
    PortfolioListForAdminResponse toPortfolioListForAdminResponse(Portfolio portfolio);

    // Portfolio -> PortfolioForAdminResponse (엔티티 -> DTO)
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.nickname", target = "nickname")
    @Mapping(source = "member.phone", target = "phone")
    @Mapping(source = "member.profileUrl", target = "profileUrl")
    @Mapping(source = "member.memberType", target = "memberType")
    PortfolioForAdminResponse toPortfolioForAdminResponse(Portfolio portfolio);
  

    /*
    ======================================신청서======================================
     */

    // List<HopeAreaRequest> -> List<HopeArea> (DTO 리스트 -> 엔티티 리스트)
    List<HopeArea> toHopeAreaList(List<HopeAreaRequest> hopeAreaRequestList);

    // HopeArea -> HopeAreaResponse (엔티티 -> DTO)
    HopeAreaResponse toHopeAreaResponse(HopeArea hopeArea);

    // List<HopeArea> -> List<HopeAreaResponse> (엔티티 리스트 -> DTO 리스트)
    List<HopeAreaResponse> toHopeAreaResponseList(List<HopeArea> hopeAreaList);

    // ApplicationForm -> ApplicationFormInfoResponse (엔티티 -> DTO)
    @Mapping(source = "client.memberId", target = "clientId")
    @Mapping(source = "agent.memberId", target = "agentId")
    @Mapping(source = "concert.concertId", target = "concertId")
    @Mapping(source = "hopeAreaList", target = "hopeAreaResponseList")
    ApplicationFormInfoResponse toApplicationFormInfoResponse(ApplicationForm applicationForm);
  
  
    /*
    ======================================FCM======================================
     */
  
  
    // FcmToken -> FcmTokenSaveResponse
    FcmTokenSaveResponse toFcmTokenSaveResponse(FcmToken fcmToken);
}
