package com.ticketmate.backend.review.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.common.core.util.CommonUtil;
import com.ticketmate.backend.common.infrastructure.util.TimeUtil;
import com.ticketmate.backend.fulfillmentform.application.service.FulfillmentFormService;
import com.ticketmate.backend.fulfillmentform.core.constant.FulfillmentFormStatus;
import com.ticketmate.backend.fulfillmentform.infrastructure.entity.FulfillmentForm;
import com.ticketmate.backend.member.application.service.AgentPerformanceService;
import com.ticketmate.backend.member.application.service.MemberService;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.review.application.dto.request.AgentCommentRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewEditRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewFilteredRequest;
import com.ticketmate.backend.review.application.dto.request.ReviewRequest;
import com.ticketmate.backend.review.application.dto.response.ReviewFilteredResponse;
import com.ticketmate.backend.review.application.dto.response.ReviewInfoResponse;
import com.ticketmate.backend.review.application.mapper.ReviewMapper;
import com.ticketmate.backend.review.infrastructure.constant.ReviewConstants;
import com.ticketmate.backend.review.infrastructure.entity.Review;
import com.ticketmate.backend.review.infrastructure.entity.ReviewImg;
import com.ticketmate.backend.review.infrastructure.repository.ReviewImgRepository;
import com.ticketmate.backend.review.infrastructure.repository.ReviewRepository;
import com.ticketmate.backend.storage.application.service.S3Service;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final S3Service s3Service;
  private final ReviewRepository reviewRepository;
  private final ReviewImgRepository reviewImgRepository;
  private final ReviewMapper reviewMapper;
  private final AgentPerformanceService agentPerformanceService;
  private final MemberService memberService;
  private final FulfillmentFormService fulfillmentFormService;

  @Transactional(readOnly = true)
  public ReviewInfoResponse getReview(UUID reviewId) {
    Review review = findReviewById(reviewId);
    return reviewMapper.toReviewInfoResponse(review);
  }

  @Transactional(readOnly = true)
  public Page<ReviewFilteredResponse> getReviewsByAgent(ReviewFilteredRequest request) {
    // 대리인 조회 및 검증
    Member agent = memberService.findMemberById(request.getAgentId());
    memberService.validateMemberType(agent, MemberType.AGENT);

    return reviewRepository.findByAgent(agent, request.toPageable())
        .map(reviewMapper::toReviewFilteredResponse);
  }

  @Transactional
  public UUID createReview(ReviewRequest request, Member member) {
    // 신청서 조회 및 검증
    FulfillmentForm fulfillmentForm = fulfillmentFormService.findFulfillmentFormById(request.getFulfillmentFormId());
    validateReviewer(fulfillmentForm, member);
    validateReviewable(fulfillmentForm);

    // 이미지 파일 개수 검증
    validateImageCount(request.getReviewImgList(), 0, 0);

    Review review = Review.create(fulfillmentForm, fulfillmentForm.getClient(), fulfillmentForm.getAgent(), request.getRating(), request.getComment());

    addReviewImages(request.getReviewImgList(), review);

    UUID reviewId = reviewRepository.save(review).getReviewId();

    try {
      // 대리인 통계 업데이트
      agentPerformanceService.addReviewStats(review.getAgent(), request.getRating());
    } catch (Exception e) {
      log.error("리뷰 생성 후 대리인 통계 업데이트에 실패했습니다. {}", e.getMessage(), e);
    }

    return reviewId;
  }

  @Transactional
  public void updateReview(UUID reviewId, ReviewEditRequest request, Member member) {
    Review review = findReviewById(reviewId);

    double oldRating = review.getRating();

    // 수정 권한 검증
    validateEditor(review, member);

    // 리뷰 수정 기간 검증
    validateEditPeriod(review, member);

    // 이미지 파일 개수 검증
    List<UUID> deleteIdList = request.getDeleteImgIdList();
    List<ReviewImg> imagesToDelete = new ArrayList<>();
    if (!CommonUtil.nullOrEmpty(deleteIdList)) {
      imagesToDelete = reviewImgRepository.findAllById(deleteIdList);
    }
    validateImageCount(request.getNewReviewImgList(), review.getReviewImgList().size(), imagesToDelete.size());

    // 기존 이미지 삭제
    if (!imagesToDelete.isEmpty()) {
      deleteImages(imagesToDelete, review);
    }

    addReviewImages(request.getNewReviewImgList(), review);

    // 리뷰 내용 업데이트
    review.update(request.getRating(), request.getComment());

    try {
      // 대리인 통계 업데이트
      agentPerformanceService.updateReviewStats(review.getAgent(), oldRating, request.getRating());
    } catch (Exception e) {
      log.error("리뷰 수정 후 대리인 통계 업데이트에 실패했습니다. {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void deleteReview(UUID reviewId, Member member) {
    Review review = findReviewById(reviewId);

    // 삭제 권한 검증
    validateEditor(review, member);

    // s3에서 이미지 삭제
    List<ReviewImg> imagesToDelete = review.getReviewImgList();
    deleteImages(imagesToDelete, review);

    try {
      // 대리인 통계 업데이트
      agentPerformanceService.deleteReviewStats(review.getAgent(), review.getRating());
    } catch (Exception e) {
      log.error("리뷰 삭제 후 대리인 통계 업데이트에 실패했습니다. {}", e.getMessage(), e);
    }

    // TODO: Soft Delete를 통한 삭제
  }

  @Transactional
  public void addAgentComment(UUID reviewId, AgentCommentRequest request, Member agent) {
    Review review = findReviewById(reviewId);

    // 리뷰 작성 권한 검증
    validateReviewCommenter(review, agent);

    // 대리인 댓글 업데이트
    review.addAgentComment(request.getComment());
  }

  private Review findReviewById(UUID reviewId) {
    return reviewRepository.findById(reviewId)
        .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
  }

  /**
   * 리뷰 댓글 작성자가 실제 리뷰의 대리인인지 검증
   */
  private void validateReviewCommenter(Review review, Member agent) {
    if (!review.getAgent().getMemberId().equals(agent.getMemberId())) {
      log.error("리뷰 댓글 작성 권한이 없습니다. reviewId={}, agentId={}", review.getReviewId(), agent.getMemberId());
      throw new CustomException(ErrorCode.NO_AUTH_TO_REVIEW_COMMENT);
    }
  }

  /**
   * 리뷰 작성자가 성공양식의 의뢰인인지 검증
   */
  private void validateReviewer(FulfillmentForm fulfillmentForm, Member client) {
    if (!fulfillmentForm.getClient().getMemberId().equals(client.getMemberId())) {
      log.error("리뷰 작성 권한이 없습니다. fulfillmentFormId={}, clientId={}", fulfillmentForm.getFulfillmentFormId(), client.getMemberId());
      throw new CustomException(ErrorCode.NO_AUTH_TO_REVIEW);
    }
  }

  /**
   * 리뷰 수정/삭제 권한이 있는지 검증
   */
  private void validateEditor(Review review, Member client) {
    if (!review.getClient().getMemberId().equals(client.getMemberId())) {
      log.error("리뷰 수정/삭제 권한이 없습니다. reviewId={}, clientId={}", review.getReviewId(), client.getMemberId());
      throw new CustomException(ErrorCode.NO_AUTH_TO_EDIT_REVIEW);
    }
  }

  /**
   * 리뷰 수정 기간(30일)이 만료되었는지 검증
   */
  private void validateEditPeriod(Review review, Member member) {
    Instant oneMonthAgo = TimeUtil.now().minus(30, ChronoUnit.DAYS);

    if (review.getCreatedDate().isBefore(oneMonthAgo)) {
      log.error("리뷰 수정 기간이 만료되었습니다. reviewId={}, memberId={}",
          review.getReviewId(), member.getMemberId());
      throw new CustomException(ErrorCode.REVIEW_EDIT_PERIOD_EXPIRED);
    }
  }

  /**
   * 성공 여부, 중복 여부 검증
   */
  private void validateReviewable(FulfillmentForm fulfillmentForm) {
    // 성공 상태의 신청서가 아니면 리뷰 작성 불가
    if (!fulfillmentForm.getFulfillmentFormStatus().equals(FulfillmentFormStatus.ACCEPTED_FULFILLMENT_FORM)) {
      log.error("리뷰 작성이 불가능한 상태입니다. fulfillmentFormId={}, status={}",
          fulfillmentForm.getFulfillmentFormId(),
          fulfillmentForm.getFulfillmentFormStatus());
      throw new CustomException(ErrorCode.CANNOT_REVIEW_NOT_SUCCEEDED_FORM);
    }
    // 이미 해당 신청서에 대한 리뷰가 존재하는지 확인
    if (reviewRepository.existsByFulfillmentForm(fulfillmentForm)) {
      log.error("이미 해당 티켓팅에 대한 리뷰가 존재합니다. fulfillmentFormId={}", fulfillmentForm.getFulfillmentFormId());
      throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
  }

  /**
   * 이미지 파일 개수 검증
   */
  private void validateImageCount(List<MultipartFile> imageFileList, int currentFileCount, int deleteFileCount) {
    int newCount = (imageFileList == null ? 0 : imageFileList.size());
    int total = currentFileCount - deleteFileCount + newCount;

    if (total > ReviewConstants.MAX_IMAGE_COUNT) {
      log.error("리뷰 이미지 업로드 개수를 초과했습니다. total={}, max={}", total, ReviewConstants.MAX_IMAGE_COUNT);
      throw new CustomException(ErrorCode.IMAGE_UPLOAD_LIMIT_EXCEEDED);
    }
  }

  /**
   * S3에 이미지들을 업로드하고, 실패 시 롤백
   */
  private List<FileMetadata> uploadImages(List<MultipartFile> imageFiles) {
    List<FileMetadata> storedPaths = new ArrayList<>();
    try {
      for (MultipartFile file : imageFiles) {
        storedPaths.add(s3Service.uploadFile(file, UploadType.REVIEW));
      }
    } catch (Exception e) {
      storedPaths.forEach(path -> s3Service.safeDeleteFile(path.storedPath()));
      log.error("리뷰 이미지 업로드 중 오류가 발생했습니다: {}, 업로드된 {}개 파일을 롤백합니다.", e.getMessage(), storedPaths.size(), e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }
    return storedPaths;
  }

  /**
   * 이미지 업로드 (S3, DB)
   */
  private void addReviewImages(List<MultipartFile> imageFileList, Review review) {
    if (CommonUtil.nullOrEmpty(imageFileList)) {
      return;
    }
    // S3에 이미지 업로드
    List<FileMetadata> storedPathList = uploadImages(imageFileList);

    // Review에 ReviewImg 추가
    List<ReviewImg> newReviewImageList = storedPathList.stream()
        .map(path -> ReviewImg.create(review, path))
        .collect(Collectors.toList());
    review.addReviewImgs(newReviewImageList);
  }

  /**
   * S3와 DB에서 이미지 삭제
   */
  private void deleteImages(List<ReviewImg> deleteImgList, Review review) {
    if (deleteImgList == null || deleteImgList.isEmpty()) return;

    // S3에서 파일 삭제
    deleteImgList.forEach(img -> s3Service.safeDeleteFile(img.getStoredPath()));

    deleteImgList.forEach(review::removeReviewImg);
    reviewImgRepository.deleteAll(deleteImgList);
  }
}
