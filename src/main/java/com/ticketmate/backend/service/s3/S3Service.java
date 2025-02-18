package com.ticketmate.backend.service.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    @Value("${cloud.aws.s3.path.portfolio.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.path.portfolio.cloud-front-domain}")
    private String domain;
    private final AmazonS3 amazonS3;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".JPG", ".JPEG", ".PNG");


    /**
     * 이미지파일을 버킷에 업로드하고 저장한 이미지파일의 이름(랜덤한 UUID가 붙은)을 반환해주는 메서드입니다.
     */
    public String s3UploadImgForCloudFront(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        // 이미지파일 확장자 검증
        validateImgFileExtension(originalFilename);

        // 중복이 되지 않는 고유한 파일이름 생성
        String randomFilename = generateRandomFilename(originalFilename);

        log.debug("filename: {}", randomFilename);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // 파일 업로드
        try {
            amazonS3.putObject(bucket, randomFilename, file.getInputStream(), metadata);

        } catch (AmazonServiceException | IOException e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
        }

        String s3UrlString = amazonS3.getUrl(bucket, randomFilename).toString();
        log.debug("S3URL: {}", s3UrlString);
        String cloudFrontUrl = domain + randomFilename;

        // 최종 이미지 URL 반환
        log.debug("ImgURL: {}", cloudFrontUrl);

        return randomFilename;
    }

    /**
     * 파일 이름 생성을 위한 메서드입니다.
     */
    public String generateRandomFilename(String fileName) {
        return UUID.randomUUID() + fileName;
    }

    /**
     * 이미지 파일만 저장될 수 있도록 파일의 유효성을 검사하는 메서드입니다.
     */
    private void validateImgFileExtension(String fileName) {
        if (fileName.length() == 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_IMAGE);
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_IMAGE);
        }

        String extension = fileName.substring(lastDot);

        // 유효 확장자 목록에 없으면 예외
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
    }
}
