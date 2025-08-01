package com.ticketmate.backend.storage.infrastructure.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class FileUtil {

  /**
   * Multipartfile이 null이거나 빈 파일인지 체크
   */
  public boolean isNullOrEmpty(MultipartFile file) {
    return file == null || file.isEmpty() || file.getOriginalFilename() == null;
  }

}
