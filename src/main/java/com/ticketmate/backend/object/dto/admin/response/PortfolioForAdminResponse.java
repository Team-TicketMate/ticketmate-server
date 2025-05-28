package com.ticketmate.backend.object.dto.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.MemberType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ToString
@NoArgsConstructor
@Getter
public class PortfolioForAdminResponse {
    private UUID portfolioId;

    private UUID memberId;

    private String nickname;

    private String phone;

    private String profileUrl;

    private MemberType memberType;

    private String portfolioDescription;

    private List<String> portfolioImgList;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedDate;

    public void addPortfolioImg(List<String> portfolioImgList) {
        this.portfolioImgList = portfolioImgList;
    }

    @Builder
    public PortfolioForAdminResponse(UUID portfolioId, UUID memberId, String nickname, String phone, String profileUrl, MemberType memberType, String portfolioDescription, List<String> portfolioImgList, LocalDateTime createdDate, LocalDateTime updatedDate) {

        this.portfolioId = portfolioId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.phone = phone;
        this.profileUrl = profileUrl;
        this.memberType = memberType;
        this.portfolioDescription = portfolioDescription;
        this.portfolioImgList = portfolioImgList;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
}
