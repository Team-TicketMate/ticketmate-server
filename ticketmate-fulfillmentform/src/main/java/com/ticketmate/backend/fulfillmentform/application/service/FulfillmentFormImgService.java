package com.ticketmate.backend.fulfillmentform.application.service;

import static com.ticketmate.backend.common.core.constant.ValidationConstants.FulfillmentForm.FULFILLMENT_IMG_MAX_COUNT;
import static com.ticketmate.backend.common.core.util.CommonUtil.nullOrEmpty;

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
import java.util.function.Consumer;
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
  public void saveFulfillmentImgInfo(FulfillmentForm fulfillmentForm, List<MultipartFile> imgList) {
    validateFulfillmentFormImgCount(imgList);

    List<FileMetadata> metadataList = uploadFulfillmentImageListWithRollback(imgList, "성공양식 이미지 업로드",
      meta -> storageService.deleteFile(meta.storedPath())
    );

    attachImagesToForm(fulfillmentForm, metadataList);

    log.debug("성공양식 이미지 {}개 업로드 성공", metadataList.size());
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

    List<FulfillmentFormImg> deleteTargetImgList = resolveDeleteTargetImageList(normalizedDeleteIdList, currentImgMapById, fulfillmentForm);

    // 총 개수 검증: 현재 - 삭제 + 추가 ≤ MAX (6개)
    int currentCount = currentImgEntityList.size();
    int deleteCount = deleteTargetImgList.size();
    int addCount = normalizedNewImageFileList.size();

    validateTotalCount(currentCount, deleteCount, addCount);

    // 신규 파일 업로드
    List<FileMetadata> uploadedMetadataList = uploadFulfillmentImageListWithRollback(
      normalizedNewImageFileList,
      "성공양식 이미지 업로드(수정)",
      meta -> safeDeleteStorage(meta.storedPath())
    );

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

  /**
   * 이미지 업로드 및 실패 시 이미 업로드된 파일 롤백
   */
  private List<FileMetadata> uploadFulfillmentImageListWithRollback(List<MultipartFile> imgList, String logContextMessage,
    Consumer<FileMetadata> deleteStrategy) {
    List<FileMetadata> metadataList = new ArrayList<>();

    if (nullOrEmpty(imgList)) {
      return metadataList;
    }

    try {
      for (MultipartFile file : imgList) {
        FileMetadata metadata = storageService.uploadFile(file, UploadType.FULFILLMENT_FORM);
        metadataList.add(metadata);
      }
    } catch (Exception e) {
      log.error("{} 중 오류 발생: {}", logContextMessage, e.getMessage(), e);
      log.error("이미 업로드 된 {}개 파일 삭제 시도", metadataList.size());
      metadataList.forEach(deleteStrategy);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }

    return metadataList;
  }

  /**
   * FileMetadata 리스트를 FulfillmentFormImg 엔티티로 변경
   */
  private void attachImagesToForm(FulfillmentForm fulfillmentForm, List<FileMetadata> metadataList) {
    for (FileMetadata metadata : metadataList) {
      FulfillmentFormImg imgEntity = FulfillmentFormImg.of(fulfillmentForm, metadata);
      fulfillmentForm.addFulfillmentFormImg(imgEntity);
    }
  }

  /**
   * 삭제 요청된 이미지 ID 리스트에 대해 실제로 해당 폼이 소유한 이미지인지 검증후 삭제 대상 엔티티 리스트를 반환
   */
  private List<FulfillmentFormImg> resolveDeleteTargetImageList(List<UUID> deleteImgIdList,
    Map<UUID, FulfillmentFormImg> currentImgMapById,
    FulfillmentForm fulfillmentForm) {
    if (nullOrEmpty(deleteImgIdList)) {
      return List.of();
    }

    List<FulfillmentFormImg> deleteTargetImgList = new ArrayList<>();

    for (UUID imgId : deleteImgIdList) {
      FulfillmentFormImg ownedImg = currentImgMapById.get(imgId);
      if (ownedImg == null) {
        log.error("성공양식 소유가 아닌 이미지 삭제 요청 imageId: {}, fulfillmentFormId: {}",
          imgId, fulfillmentForm.getFulfillmentFormId());
        throw new CustomException(ErrorCode.FULFILLMENT_IMAGE_NOT_OWNED_BY_FORM);
      }
      deleteTargetImgList.add(ownedImg);
    }

    return deleteTargetImgList;
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

    if (finalCount > FULFILLMENT_IMG_MAX_COUNT) {
      log.error("이미지 개수 초과: 현재={}, 삭제={}, 추가={} -> 최종={}", currentCount, deleteCount, addCount, finalCount);
      throw new CustomException(ErrorCode.INVALID_FULFILLMENT_FORM_IMG_COUNT);
    }
  }

  /**
   * 요청된 첨부파일 개수 검증
   */
  private void validateFulfillmentFormImgCount(List<MultipartFile> imgList) {
    if (imgList.size() > FULFILLMENT_IMG_MAX_COUNT) {
      log.error("성공양식 이미지 첨부파일은 최대 6개까지 등록 가능합니다. 요청개수: {}", imgList.size());
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
