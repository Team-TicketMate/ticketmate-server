package com.ticketmate.backend.object.dto.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ticketmate.backend.object.constants.PortfolioType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class PortfolioFilteredAdminResponse {
    private UUID portfolioId;

    private UUID memberId;

    private String username;

    private String nickname;

    private String name;

    private PortfolioType portfolioType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedDate;
}
