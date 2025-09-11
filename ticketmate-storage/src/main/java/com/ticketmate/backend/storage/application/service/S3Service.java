package com.ticketmate.backend.storage.application.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.storage.core.constant.FileExtension;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import com.ticketmate.backend.storage.core.service.StorageService;
import com.ticketmate.backend.storage.infrastructure.properties.S3Properties;
import com.ticketmate.backend.storage.infrastructure.util.FileUtil;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service implements StorageService {

  // 날짜 포멧: yyyyMMdd
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final AmazonS3 amazonS3;
  private final S3Properties s3Properties;
  private final Clock clock;
  private final ZoneId zoneId;

  /**
   * 파일 검증 및 원본 파일명 반환
   *
   * @param file 요청된 MultipartFile
   * @return 원본 파일명
   */
  private static String validateAndExtractFilename(MultipartFile file) {
    // 파일 검증
    if (FileUtil.isNullOrEmpty(file)) {
      log.error("파일이 비어있거나 존재하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }

    // 원본 파일 명 검증
    String originalFilename = file.getOriginalFilename();
    if (CommonUtil.nvl(originalFilename, "").isEmpty()) {
      log.error("원본 파일명이 비어있거나 존재하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }
    return originalFilename;
  }

  @Override
  @Transactional
  public FileMetadata uploadFile(MultipartFile file, UploadType uploadType) {

    // 파일 검증 및 원본 파일명 추출
    String originalFilename = validateAndExtractFilename(file);

    // 파일 확장자 검증 및 확장자 가져오기
    FileExtension fileExtension = FileExtension.fromFilename(originalFilename);

    // 파일 prefix 설정
    String prefix = determinePrefix(uploadType);

    // 중복이 되지 않는 고유한 파일이름 생성
    String storedPath = generateStoredPath(originalFilename, prefix);
    log.debug("생성된 파일명: {}", storedPath);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(fileExtension.getContentType());

    // S3 파일 업로드
    try (InputStream inputStream = file.getInputStream()) {
      amazonS3.putObject(s3Properties.s3().bucket(), storedPath, inputStream, metadata);
      log.debug("S3 파일 업로드 성공: {}", storedPath);
    } catch (AmazonServiceException ase) {
      log.error("AmazonServiceException - S3 파일 업로드 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Properties.s3().bucket(), storedPath, ase.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_AMAZON_SERVICE_ERROR);
    } catch (AmazonClientException ace) {
      log.error("AmazonClientException - S3 클라이언트 에러 발생. 버킷: {}, 파일명: {}, 에러: {}", s3Properties.s3().bucket(), storedPath, ace.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_AMAZON_CLIENT_ERROR);
    } catch (IOException ioe) {
      log.error("IOException - 파일 스트림 처리 중 에러 발생. 원본 파일명: {}, 파일명: {} 에러: {}", originalFilename, storedPath, ioe.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }

    // FileMetadata 반환
    return new FileMetadata(originalFilename, storedPath, fileExtension, file.getSize());
  }

  @Override
  public String generatePublicUrl(String storedPath) {
    if (storedPath.startsWith("https://picsum.photos/")) {
      return storedPath;
    } // TODO: Mock 데이터 사진은 "https://picsum.photos/..." 고정이므로 Mock 데이터는 storedPath를 저장하는 것이 아닌 publicUrl 전체 저장 (개발 후 삭제 필요)
    if (CommonUtil.nvl(storedPath, "").isEmpty()) {
      return null;
    }
    return FileUtil.combineBaseAndPath(s3Properties.s3().domain(), storedPath); // 예: "https://domain.com/prefix/yyyyMMdd-UUID-파일명.jpg"
  }

  @Override
  public String extractStoredPathFromPublicUrl(String publicUrl) {
    String storedPath = "";
    if (publicUrl.startsWith(s3Properties.s3().domain())) {
      storedPath = publicUrl.substring(s3Properties.s3().domain().length());
    }
    if (CommonUtil.nvl(storedPath, "").isEmpty()) {
      log.error("파일 저장 경로 추출 실패: {}", publicUrl);
      throw new CustomException(ErrorCode.INVALID_PUBLIC_URL);
    }
    log.debug("파일 저장 경로 추출 성공: {}", storedPath);
    return storedPath;
  }

  @Override
  @Transactional
  public void deleteFile(String storedPath) {
    if (storedPath.startsWith("https://picsum.photos/")) {
      return;
    } // TODO: Mock 데이터 사진은 "https://picsum.photos/..." 고정이므로 Mock 데이터는 storedPath를 저장하는 것이 아닌 publicUrl 전체 저장 (개발 후 삭제 필요)

    if (CommonUtil.nvl(storedPath, "").isEmpty()) {
      log.warn("요청된 파일 경로가 없습니다.");
      return;
    }

    // S3 파일 삭제
    try {
      amazonS3.deleteObject(new DeleteObjectRequest(s3Properties.s3().bucket(), storedPath));
      log.debug("S3 파일 삭제 성공: {}", storedPath);
    } catch (AmazonServiceException ase) {
      log.error("AmazonServiceException - S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Properties.s3().bucket(), storedPath, ase.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_AMAZON_SERVICE_ERROR);
    } catch (AmazonClientException ace) {
      log.error("AmazonClientException - S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Properties.s3().bucket(), storedPath, ace.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_AMAZON_CLIENT_ERROR);
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Properties.s3().bucket(), storedPath, e.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_ERROR);
    }
  }

  /**
   * 저장 경로(storedPath) 생성 (prefix/yyyyMMdd-UUID-파일명.jpg)
   *
   * @param originalFilename 원본 파일명
   * @return 저장 경로(storedPath) (prefix/yyyyMMdd-UUID-파일명.jpg)
   */
  private String generateStoredPath(String originalFilename, String prefix) {
    String datePart = ZonedDateTime.now(zoneId).format(DATE_TIME_FORMATTER);
    // 파일명에서 경로 구분자와 특수 문자 제거 + 공백류를 언더스코어로 치환
    String sanitizedFilename = originalFilename
        .replaceAll("[/\\\\:*?\"<>|]", "_")
        .replaceAll("\\s+", "_");
    return String.format("%s/%s-%s-%s", prefix, datePart, UUID.randomUUID(), sanitizedFilename);
  }

  /**
   * FileUploadType에 따라 S3 내부에 붙일 prefix(=하위폴더) 반환
   */
  private String determinePrefix(UploadType type) {
    return switch (type) {
      case MEMBER -> s3Properties.s3().path().member();
      case CONCERT_HALL -> s3Properties.s3().path().concertHall();
      case CONCERT -> s3Properties.s3().path().concert();
      case PORTFOLIO -> s3Properties.s3().path().portfolio();
      case CHAT -> s3Properties.s3().path().chat();
    };
  }

  /**
   * S3에 파일은 업로드 되어있지만 서버 내부적으로 오류 발생시 이미지 롤백(삭제)
   */
  public void safeDeleteFile(String storedPath) {
    try {
      deleteFile(storedPath);
    } catch (Exception ex) {
      log.warn("S3 파일 롤백 실패: {}", storedPath, ex);
    }
  }
}
