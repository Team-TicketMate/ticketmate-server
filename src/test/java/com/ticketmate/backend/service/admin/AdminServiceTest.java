package com.ticketmate.backend.service.admin;

import com.ticketmate.backend.object.dto.admin.request.ConcertDateRequest;
import com.ticketmate.backend.object.dto.admin.request.ConcertInfoEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class AdminServiceTest {

    @Autowired
    AdminService adminService;

    UUID concertId = UUID.fromString("2648d054-a114-4a76-ae78-1e9131bb42b3");

    ConcertInfoEditRequest concertInfoEditRequest = new ConcertInfoEditRequest();

    List<ConcertDateRequest> concertDateRequestList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 기준 날짜 (한달 뒤)
        LocalDateTime rootDate = LocalDateTime.now().plusMonths(1);

        // 공연날짜 3개 저장
        for (int i = 0; i < 3; i++) {
            ConcertDateRequest concertDateRequest = ConcertDateRequest.builder()
                    .performanceDate(rootDate.plusDays(i))
                    .session(i + 1) // 1회차, 2회차, 3회차
                    .build();
            concertDateRequestList.add(concertDateRequest);
        }
        concertInfoEditRequest.setConcertDateRequestList(concertDateRequestList);
        log.debug("공연 정보 DTO 저장 성공: {}", concertDateRequestList.size());
    }

    @Test
    void 공연일자_변경_성공() {
        log.debug("공연일자 변경 테스트 시작");
        adminService.editConcertInfo(concertId, concertInfoEditRequest);
        log.debug("공연일자 변경 테스트 종료");
    }
}