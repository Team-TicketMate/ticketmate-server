package com.ticketmate.backend.storage.core.service;

import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  /**
   * 파일 업로드
   * 사용자로부터 요청된 MultipartFile을 업로드 후 storedPath 반환
   *
   * @param file       업로드할 MultipartFile
   * @param uploadType 업로드할 파일의 도메인 구분
   * @return {@link FileMetadata} FileMetadata
   */
  FileMetadata uploadFile(MultipartFile file, UploadType uploadType);

  /**
   * 파일 저장 경로 storedPath 로부터 접근 가능 publicUrl 생성
   *
   * @param storedPath 파일 저장 경로 (prefix/yyyyDDmm-UUID-파일명.jpg)
   * @return 접근 가능 publicUrl
   */
  String generatePublicUrl(String storedPath);

  /**
   * publicUrl 에서 파일 저장 경로(storedPath) 추출
   *
   * @param publicUrl 접근 가능한 URL
   * @return storedPath
   */
  String extractStoredPathFromPublicUrl(String publicUrl);

  /**
   * 파일 삭제
   *
   * @param storedPath 삭제할 파일의 저장 경로(storedPath)
   */
  void deleteFile(String storedPath);
}
