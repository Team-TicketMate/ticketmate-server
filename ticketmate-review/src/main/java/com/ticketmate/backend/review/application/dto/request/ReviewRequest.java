package com.ticketmate.backend.review.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReviewRequest {
  @NotNull(message = "신청서 ID는 필수입니다.")
  private UUID applicationFormId;

  @NotNull(message = "별점은 필수입니다.")
  @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다.")
  @DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
  private Float rating;

  @NotBlank(message = "리뷰 내용은 필수입니다.")
  @Size(min = 10, max = 300, message = "리뷰 내용은 10자 이상 300자 이하로 작성해야 합니다.")
  private String comment;

  @Size(max = 3, message = "리뷰 첨부파일은 최대 3개까지 등록 가능합니다.")
  private List<MultipartFile> reviewImgList = new ArrayList<>();
}
