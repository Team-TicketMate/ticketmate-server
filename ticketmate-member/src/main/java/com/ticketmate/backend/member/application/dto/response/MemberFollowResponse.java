package com.ticketmate.backend.member.application.dto.response;

public record MemberFollowResponse(
    String nickname,
    String profileUrl,
    long followerCount
) {

}
