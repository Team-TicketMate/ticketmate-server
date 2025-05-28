package com.ticketmate.backend.object.dto.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedDate;
}
