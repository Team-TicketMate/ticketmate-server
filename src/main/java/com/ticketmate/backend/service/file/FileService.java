package com.ticketmate.backend.service.file;

import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    @Value("${server.file.path.concert.thumbnail}")
    private String uploadPath;

    @Value("${server.file.domain}")
    private String fileUrlPath;

    /**
     * 파일 리스트 저장
     *
     * @param multipartFileList
     * @return 저장된 파일 URL List
     */
    public List<String> saveFiles(List<MultipartFile> multipartFileList) {
        List<String> fileUrlList = new ArrayList<>();

        multipartFileList.forEach(multipartFile ->
                fileUrlList.add(saveFile(multipartFile)));

        return fileUrlList;
    }

    /**
     * 단일 파일 저장
     *
     * @param multipartFile
     * @return 저장된 파일 URL
     */
    public String saveFile(MultipartFile multipartFile) {

        // 원본 파일명
        String originalFileName = multipartFile.getOriginalFilename();
        log.debug("업로드할 원본 파일명: {}", originalFileName);

        // 고유 파일명 생성
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

        // 경로 설정
        Path filePath = Paths.get(uploadPath, uniqueFileName);
        log.debug("파일 저장 경로: {}", filePath);

        // 파일 저장
        try {
            log.debug("파일 업로드 시작");
            multipartFile.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("파일 업로드 중 문제가 발생했습니다.", e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        // 파일 URL 생성
        String fileUrl = fileUrlPath + uniqueFileName;
        log.debug("파일 업로드 성공: 업로드 파일 url={}", fileUrl);
        return fileUrl;
    }
}
