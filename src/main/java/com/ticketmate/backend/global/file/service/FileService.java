package com.ticketmate.backend.global.file.service;

import com.ticketmate.backend.global.file.constant.UploadType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

  /**
   * 파일 업로드
   *
   * @param file       업로드할 MultipartFile
   * @param uploadType 업로드할 파일의 도메인 구분
   * @return 업로드된 파일에 접근 가능한 URL
   */
  String uploadFile(MultipartFile file, UploadType uploadType);

  /**
   * 파일 삭제
   *
   * @param filePath 삭제할 파일의 URL
   */
  void deleteFile(String filePath);
}
