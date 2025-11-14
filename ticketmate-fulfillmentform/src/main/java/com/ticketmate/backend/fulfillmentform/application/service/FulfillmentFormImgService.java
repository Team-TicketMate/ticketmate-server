package com.ticketmate.backend.fulfillmentform.application.service;

import static com.ticketmate.backend.common.core.util.CommonUtil.nullOrEmpty;
import static com.ticketmate.backend.fulfillmentform.infrastructure.constant.FulfillmentFormConstants.MAX_IMG_COUNT;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentFormImg;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class FulfillmentFormImgService {

  private final StorageService storageService;

  @Transactional
  public FulfillmentForm saveFulfillmentImgInfo(FulfillmentForm fulfillmentForm, List<MultipartFile> imgList) {
    List<FileMetadata> metadataList = new ArrayList<>();

    validateFulfillmentFormImgCount(imgList);

    try {
      for (MultipartFile file : imgList) {
        FileMetadata metadata = storageService.uploadFile(file, UploadType.FULFILLMENT_FORM);
        metadataList.add(metadata);
      }
    } catch (Exception e) {
      log.error("성공양식 이미지 업로드 중 오류 발생: {}", e.getMessage());
      log.error("이미 업로드 된 {} 개 파일 삭제 시도", metadataList.size());
      metadataList.forEach(metadata -> storageService.deleteFile(metadata.storedPath()));
      throw new CustomException(ErrorCode.FULFILLMENT_FORM_UPLOAD_ERROR);
    }
    for (FileMetadata metadata : metadataList) {
      FulfillmentFormImg fulfillmentFormImg = FulfillmentFormImg.of(fulfillmentForm, metadata);
      fulfillmentForm.addFulfillmentFormImg(fulfillmentFormImg);
    }
    log.debug("성공양식 이미지 {}개 업로드 성공", metadataList.size());
    return fulfillmentForm;
  }

  public void updateImageListByDeleteAndAdd(FulfillmentForm fulfillmentForm, List<UUID> deleteImgIdList, List<MultipartFile> newSuccessImgList) {
    List<UUID> normalizedDeleteIdList = normalizeDeleteIdList(deleteImgIdList);
    List<MultipartFile> normalizedNewImageFileList = normalizeNewImageFileList(newSuccessImgList);

    if (nullOrEmpty(normalizedDeleteIdList) && nullOrEmpty(normalizedNewImageFileList)) {
      return;
    }

    List<FulfillmentFormImg> currentImgEntityList = fulfillmentForm.getSuccessTicketingStoredPathList();

    Map<UUID, FulfillmentFormImg> currentImgMapById = currentImgEntityList.stream()
      .collect(Collectors.toMap(FulfillmentFormImg::getFulfillmentFormImgId, it -> it));

    List<FulfillmentFormImg> deleteTargetImgList = new ArrayList<>();

    if (!nullOrEmpty(normalizedDeleteIdList)) {
      for (UUID imgId : normalizedDeleteIdList) {
        FulfillmentFormImg ownedImg = currentImgMapById.get(imgId);
        if (ownedImg == null) {
          log.error("성공양식 소유가 아닌 이미지 삭제 요청 imageId: {}, fulfillmentFormId: {}",
            imgId, fulfillmentForm.getFulfillmentFormId());
          throw new CustomException(ErrorCode.FULFILLMENT_IMAGE_NOT_OWNED_BY_FORM);
        }
        deleteTargetImgList.add(ownedImg);
      }
    }

    // 총 개수 검증: 현재 - 삭제 + 추가 ≤ MAX (6개)
    int currentCount = currentImgEntityList.size();
    int deleteCount = deleteTargetImgList.size();
    int addCount = normalizedNewImageFileList.size();

    validateTotalCount(currentCount, deleteCount, addCount);

    // 신규 파일 업로드
    List<FileMetadata> uploadedMetadataList = new ArrayList<>();

    try {
      for (MultipartFile file : normalizedNewImageFileList) {
        FileMetadata metadata = storageService.uploadFile(file, UploadType.FULFILLMENT_FORM);
        uploadedMetadataList.add(metadata);
      }
    } catch (Exception e) {
      // 업로드 실패시 이미 업로드한 파일 정리
      uploadedMetadataList.forEach(meta -> safeDeleteStorage(meta.storedPath()));
      log.error("성공양식 이미지 업로드(수정) 실패: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }

    // 성공양식 엔티티에 연관되어 있는 필드 삭제 -> 파일은 커밋 직후 삭제
    if (!nullOrEmpty(deleteTargetImgList)) {
      List<String> storedPathListForDeletion = deleteTargetImgList.stream()
        .map(FulfillmentFormImg::getStoredPath)
        .toList();

      deleteTargetImgList.forEach(fulfillmentForm::removeFulfillmentFormImg);
      registerAfterCommitDeletion(storedPathListForDeletion);
    }

    // 신규 파일 추가
    if (!nullOrEmpty(uploadedMetadataList)) {
      for (FileMetadata metadata : uploadedMetadataList) {
        FulfillmentFormImg newImageEntity = FulfillmentFormImg.of(fulfillmentForm, metadata);
        fulfillmentForm.addFulfillmentFormImg(newImageEntity);
      }
    }
  }

  private void safeDeleteStorage(String storedPath) {
    try {
      storageService.deleteFile(storedPath);
    } catch (Exception e) {
      log.error("스토리지 파일 삭제 실패(storedPath={}): {}", storedPath, e.getMessage());
    }
  }

  private void validateTotalCount(int currentCount, int deleteCount, int addCount) {
    int finalCount = currentCount - deleteCount + addCount;

    if (finalCount > MAX_IMG_COUNT) {
      log.error("이미지 개수 초과: 현재={}, 삭제={}, 추가={} -> 최종={}", currentCount, deleteCount, addCount, finalCount);
      throw new CustomException(ErrorCode.INVALID_FULFILLMENT_FORM_IMG_COUNT);
    }
  }

  /**
   * 요청된 첨부파일 개수 검증
   */
  private void validateFulfillmentFormImgCount(List<MultipartFile> imgList) {
    if (imgList.size() > MAX_IMG_COUNT) {
      log.error("성공양식 이미지 첨부파일은 최대 6개까지 등록가능합니다. 요청개수: {}", imgList.size());
      throw new CustomException(ErrorCode.INVALID_PORTFOLIO_IMG_COUNT);
    }
  }

  private List<UUID> normalizeDeleteIdList(List<UUID> deleteImgIdList) {
    if (nullOrEmpty(deleteImgIdList)) {
      return List.of();
    }

    return deleteImgIdList.stream().filter(Objects::nonNull).distinct().toList();
  }

  private List<MultipartFile> normalizeNewImageFileList(List<MultipartFile> newSuccessImgList) {
    if (nullOrEmpty(newSuccessImgList)) {
      return List.of();
    }

    return newSuccessImgList.stream().filter(file -> file != null && !file.isEmpty()).toList();
  }

  /**
   * 커밋 이후 파일 삭제
   */
  private void registerAfterCommitDeletion(List<String> storedPathList) {
    if (nullOrEmpty(storedPathList)) {
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        for (String storedPath : storedPathList) {
          safeDeleteStorage(storedPath);
        }
      }
    });
  }
}
