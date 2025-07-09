package com.ticketmate.backend.global.file.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ticketmate.backend.global.config.S3Config;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.file.constant.FileExtension;
import com.ticketmate.backend.global.file.constant.UploadType;
import com.ticketmate.backend.global.util.common.CommonUtil;
import com.ticketmate.backend.global.util.common.FileUtil;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Primary
@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service implements FileService {

  // 날짜 포멧: yyyyMMdd
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final AmazonS3 amazonS3;
  private final S3Config s3Config;

  /**
   * S3 파일 업로드 후 파일 URL 반환
   *
   * @param file       업로드할 MultipartFile
   * @param uploadType 업로드할 파일의 도메인 구분
   * @return 업로드된 파일에 접근 가능한 URL
   */
  @Override
  public String uploadFile(MultipartFile file, UploadType uploadType) {

    // 파일 검증 및 원본 파일명 추출
    String originalFilename = validateAndExtractFilename(file);

    // 파일 확장자 검증 및 확장자 가져오기
    FileExtension fileExtension = FileExtension.fromFilename(originalFilename);

    // 파일 prefix 설정
    String prefix = determinePrefix(uploadType);

    // 중복이 되지 않는 고유한 파일이름 생성
    String filename = generateFilename(originalFilename, prefix);
    log.debug("생성된 파일명: {}", filename);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(fileExtension.getContentType());

    // S3 파일 업로드
    try (InputStream inputStream = file.getInputStream()) {
      amazonS3.putObject(s3Config.getBucket(), filename, inputStream, metadata);
      log.debug("S3 파일 업로드 성공: {}", filename);
    } catch (AmazonServiceException ase) {
      log.error("AmazonServiceException - S3 파일 업로드 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Config.getBucket(), filename, ase.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_AMAZON_SERVICE_ERROR);
    } catch (AmazonClientException ace) {
      log.error("AmazonClientException - S3 클라이언트 에러 발생. 버킷: {}, 파일명: {}, 에러: {}", s3Config.getBucket(), filename, ace.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_AMAZON_CLIENT_ERROR);
    } catch (IOException ioe) {
      log.error("IOException - 파일 스트림 처리 중 에러 발생. 원본 파일명: {}, 파일명: {} 에러: {}", originalFilename, filename, ioe.getMessage());
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }

    // 파일 URL 생성 및 반환
    return generateFilePath(filename);
  }

  /**
   * S3에서 파일 삭제
   *
   * @param filePath 삭제할 파일의 URL
   */
  @Override
  public void deleteFile(String filePath) {
    if (CommonUtil.nvl(filePath, "").isEmpty()) {
      log.warn("삭제할 파일 URL이 없습니다.");
      return;
    }

    // URL에서 도메인 제거 (filename만 추출)
    String filename = extractFilenameFromFilepath(filePath);

    // S3 파일 삭제
    try {
      amazonS3.deleteObject(new DeleteObjectRequest(s3Config.getBucket(), filename));
      log.debug("S3 파일 삭제 성공: {}", filename);
    } catch (AmazonServiceException ase) {
      log.error("AmazonServiceException - S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Config.getBucket(), filename, ase.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_AMAZON_SERVICE_ERROR);
    } catch (AmazonClientException ace) {
      log.error("AmazonClientException - S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Config.getBucket(), filename, ace.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_AMAZON_CLIENT_ERROR);
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패. 버킷: {}, 파일명: {}, 에러: {}", s3Config.getBucket(), filename, e.getMessage());
      throw new CustomException(ErrorCode.S3_DELETE_ERROR);
    }
  }

  /**
   * 파일 검증 및 원본 파일명 반환
   *
   * @param file 요청된 MultipartFile
   * @return 원본 파일명
   */
  private static String validateAndExtractFilename(MultipartFile file) {
    // 파일 검증
    if (FileUtil.isNullOrEmpty(file)) {
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }

    // 원본 파일 명 검증
    String originalFilename = file.getOriginalFilename();
    if (CommonUtil.nvl(originalFilename, "").isEmpty()) {
      throw new CustomException(ErrorCode.INVALID_FILE_REQUEST);
    }
    return originalFilename;
  }

  /**
   * 파일 명 생성 (prefix/yyyyMMdd-UUID-파일명.jpg)
   *
   * @param originalFilename 원본 파일명
   * @return UUID-originalFilename: 가공된 파일명
   */
  private String generateFilename(String originalFilename, String prefix) {
    String datePart = LocalDateTime.now().format(DATE_TIME_FORMATTER);
    // 파일명에서 경로 구분자와 특수 문자 제거
    String sanitizedFilename = originalFilename.replaceAll("[/\\\\:*?\"<>|]", "_");
    return String.format("%s/%s-%s-%s", prefix, datePart, UUID.randomUUID(), sanitizedFilename);
  }

  /**
   * 파일 URL 생성
   *
   * @param filename 파일명 (prefix/yyyyMMdd-UUID-파일명.jpg)
   * @return 파일 URL
   */
  private String generateFilePath(String filename) {
    return s3Config.getDomain() + filename; // 예: "member/20250605-a1b2c3-원본이미지.jpg"
  }

  /**
   * 파일 URL에서 파일명 추출
   *
   * @param filePath 파일 URL
   * @return filename
   */
  private String extractFilenameFromFilepath(String filePath) {
    String filename = filePath;
    if (filePath.startsWith(s3Config.getDomain())) {
      filename = filePath.substring(s3Config.getDomain().length());
    }
    if (CommonUtil.nvl(filename, "").isEmpty()) {
      log.error("파일명 추출 실패: {}", filePath);
      throw new CustomException(ErrorCode.INVALID_FILE_PATH);
    }
    return filename;
  }

  /**
   * FileUploadType에 따라 S3 내부에 붙일 prefix(=하위폴더) 반환
   */
  private String determinePrefix(UploadType type) {
    return switch (type) {
      case MEMBER -> s3Config.getMemberPrefix();
      case CONCERT_HALL -> s3Config.getConcertHallPrefix();
      case CONCERT -> s3Config.getConcertPrefix();
      case PORTFOLIO -> s3Config.getPortfolioPrefix();
    };
  }
}
