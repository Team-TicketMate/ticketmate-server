package com.ticketmate.backend.review.application.dto.request;

import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MaxErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.MinErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotBlankErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.NotNullErrorCode;
import com.ticketmate.backend.common.application.exception.annotation.SizeErrorCode;
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
  @NotNull
  @NotNullErrorCode(ErrorCode.FULFILLMENT_FORM_ID_EMPTY)
  private UUID fulfillmentFormId;

  @NotNull
  @NotNullErrorCode(ErrorCode.RATING_EMPTY)
  @DecimalMin(value = "0.0")
  @MinErrorCode(ErrorCode.RATING_TOO_LOW)
  @DecimalMax(value = "5.0")
  @MaxErrorCode(ErrorCode.RATING_TOO_HIGH)
  private Float rating;

  @NotBlank
  @NotBlankErrorCode(ErrorCode.COMMENT_EMPTY)
  @Size(min = 10, max = 300)
  @SizeErrorCode(ErrorCode.COMMENT_LENGTH_INVALID)
  private String comment;

  @Size(max = 3)
  @SizeErrorCode(ErrorCode.REVIEW_IMG_LIST_EXCEED)
  private List<MultipartFile> reviewImgList = new ArrayList<>();
}
