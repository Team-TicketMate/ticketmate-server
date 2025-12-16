package com.ticketmate.backend.review.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ReviewEditRequest {
  @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다.")
  @DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
  private Float rating;

  @Size(min = 10, max = 300, message = "comment는 최소 10자 최대 300자 입력 가능합니다")
  private String comment;

  private List<UUID> deleteImgIdList = new ArrayList<>();

  private List<MultipartFile> newReviewImgList =  new ArrayList<>();
}
