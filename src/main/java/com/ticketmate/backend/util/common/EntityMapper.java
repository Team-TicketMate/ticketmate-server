package com.ticketmate.backend.util.common;

import com.ticketmate.backend.object.dto.admin.response.PortfolioForAdminResponse;
import com.ticketmate.backend.object.dto.admin.response.PortfolioListForAdminResponse;
import com.ticketmate.backend.object.dto.concert.response.ConcertFilteredResponse;
import com.ticketmate.backend.object.dto.concerthall.response.ConcertHallFilteredResponse;
import com.ticketmate.backend.object.dto.fcm.response.FcmTokenSaveResponse;
import com.ticketmate.backend.object.postgres.concert.Concert;
import com.ticketmate.backend.object.postgres.concerthall.ConcertHall;
import com.ticketmate.backend.object.postgres.portfolio.Portfolio;
import com.ticketmate.backend.object.redis.FcmToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    // ConcertHall -> ConcertHallFilteredResponse
    ConcertHallFilteredResponse toConcertHallFilteredResponse(ConcertHall concertHall);

    // Concert -> ConcertFilteredResponse
    @Mapping(source = "concertHall.concertHallName", target = "concertHallName")
    ConcertFilteredResponse toConcertFilteredResponse(Concert concert);

    // Portfolio -> PortfolioListForAdminResponse
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.nickname", target = "nickname")
    PortfolioListForAdminResponse toPortfolioListForAdminResponse(Portfolio portfolio);

    // Portfolio -> PortfolioForAdminResponse
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.nickname", target = "nickname")
    @Mapping(source = "member.phone", target = "phone")
    @Mapping(source = "member.profileUrl", target = "profileUrl")
    @Mapping(source = "member.memberType", target = "memberType")
    PortfolioForAdminResponse toPortfolioForAdminResponse(Portfolio portfolio);

    // FcmToken -> FcmTokenSaveResponse
    FcmTokenSaveResponse toFcmTokenSaveResponse(FcmToken fcmToken);
}
