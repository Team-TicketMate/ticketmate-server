package com.match.backend.storage.core.constant;

import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileExtension {

  JPG("jpg", "image/jpeg"),

  JPEG("jpeg", "image/jpeg"),

  PNG("png", "image/png"),

  GIF("gif", "image/gif"),

  BMP("bmp", "image/bmp"),

  WEBP("webp", "image/webp");

  private final String extension;
  private final String contentType;

  /**
   * 파일명에서 확장자 추출
   *
   * @param filename 파일명
   * @return 파일 확장자
   */
  public static FileExtension fromFilename(String filename) {
    if (CommonUtil.nvl(filename, "").isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }

    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex < 0) {
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }

    String extension = filename.substring(lastDotIndex + 1).toLowerCase();

    return Arrays.stream(values())
        .filter(fileExtension -> fileExtension.getExtension().equals(extension))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_FILE_EXTENSION));
  }

  /**
   * 파일 확장자가 유효한지 검증
   *
   * @param filename 파일명
   * @return 유효한 확장자이면 true, 유효하지 않으면 false
   */
  public static boolean isValidExtension(String filename) {
    try {
      fromFilename(filename);
      return true;
    } catch (CustomException e) {
      return false;
    }
  }

  /**
   * 확장자에 해당하는 ContentType 반환
   *
   * @param filename 파일명
   * @return ContentType
   */
  public static String getContentTypeByFilename(String filename) {
    return fromFilename(filename).getContentType();
  }

}
