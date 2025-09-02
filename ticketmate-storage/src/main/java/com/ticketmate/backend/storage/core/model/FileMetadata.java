package com.ticketmate.backend.storage.core.model;

import com.ticketmate.backend.storage.core.constant.FileExtension;

public record FileMetadata(
    String originalFilename, // 원본 파일명
    String storedPath, // 파일 저장 경로
    FileExtension fileExtension, // 파일 확장자
    long sizeBytes // 파일 크기
) {

}
