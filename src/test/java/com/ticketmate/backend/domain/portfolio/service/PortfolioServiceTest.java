package com.ticketmate.backend.domain.portfolio.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.portfolio.domain.dto.request.PortfolioRequest;
import com.ticketmate.backend.domain.portfolio.domain.entity.Portfolio;
import com.ticketmate.backend.domain.portfolio.repository.PortfolioRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.service.FileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

  @InjectMocks
  PortfolioService portfolioService;

  @Mock
  PortfolioRepository portfolioRepository;

  @Mock
  FileService fileService;

  @BeforeEach
  void setUp() {
  }

  @Test
  void 포트폴리오_업로드_성공() {
    Member client = createClientMember();
    // given
    PortfolioRequest request = createPortfolioRequest("안녕하세요. 티켓팅 장인입니다.", 10);
    when(fileService.uploadFile(any(), any()))
        .thenReturn("test-file-upload-success");
    when(portfolioRepository.save(any(Portfolio.class)))
        .thenReturn(Portfolio.builder().portfolioId(UUID.randomUUID()).build());

    // when
    portfolioService.uploadPortfolio(request, client);

    // then
    Mockito.verify(portfolioRepository, times(1)).save(any(Portfolio.class));
  }

  @Test
  void 포트폴리오_첨부파일_미등록_오류() {
    Member client = createClientMember();

    // given
    PortfolioRequest request = createPortfolioRequest("안녕하세요. 티켓팅 장인입니다.", 0);

    // when

    // then
    Assertions.assertThatThrownBy(() -> portfolioService.uploadPortfolio(request, client))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_PORTFOLIO_IMG_COUNT.getMessage());
  }

  @Test
  void 포트폴리오_첨부파일_20개_초과_등록_오류() {
    Member client = createClientMember();

    // given
    PortfolioRequest request = createPortfolioRequest("안녕하세요. 티켓팅 장인입니다.", 21);

    // when

    // then
    Assertions.assertThatThrownBy(() -> portfolioService.uploadPortfolio(request, client))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ErrorCode.INVALID_PORTFOLIO_IMG_COUNT.getMessage());
  }

  private static Member createClientMember() {
    return Member.builder()
        .memberId(UUID.randomUUID())
        .memberType(MemberType.CLIENT)
        .build();
  }

  private PortfolioRequest createPortfolioRequest(String portfolioDescription, int imgCount) {
    List<MultipartFile> multipartFileList = new ArrayList<>();
    for (int i = 0; i < imgCount; i++) {
      multipartFileList.add(createMockMultipartFile());
    }
    return PortfolioRequest.builder()
        .portfolioDescription(portfolioDescription)
        .portfolioImgList(multipartFileList)
        .build();
  }

  private MockMultipartFile createMockMultipartFile() {
    String base = UUID.randomUUID().toString();
    return new MockMultipartFile(
        base,
        base + ".png",
        "image/png",
        new byte[]{1, 2, 3}
    );
  }
}