package com.ticketmate.backend.review.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.DecimalMaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.DecimalMinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
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
  @DecimalMin(value = "0.0")
  @DecimalMinErrorCode(ErrorCode.RATING_TOO_LOW)
  @DecimalMax(value = "5.0")
  @DecimalMaxErrorCode(ErrorCode.RATING_TOO_HIGH)
  private Float rating;

  @Size(min = 10, max = 300)
  @SizeErrorCode(ErrorCode.COMMENT_LENGTH_INVALID)
  private String comment;

  private List<UUID> deleteImgIdList = new ArrayList<>();

  private List<MultipartFile> newReviewImgList =  new ArrayList<>();
}
