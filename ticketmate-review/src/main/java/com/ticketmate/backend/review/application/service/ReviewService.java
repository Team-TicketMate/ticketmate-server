package com.ticketmate.backend.review.application.service;

import com.ticketmate.backend.applicationform.application.service.ApplicationFormService;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationForm;
import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
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
import com.ticketmate.backend.review.infrastructure.entity.Review;
import com.ticketmate.backend.review.infrastructure.entity.ReviewImg;
import com.ticketmate.backend.review.infrastructure.repository.ReviewImgRepository;
import com.ticketmate.backend.review.infrastructure.repository.ReviewRepository;
import com.ticketmate.backend.storage.application.service.S3Service;
import com.ticketmate.backend.storage.core.constant.UploadType;
import com.ticketmate.backend.storage.core.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
  private final S3Service s3Service;
  private final ApplicationFormService applicationFormService;
  private final ReviewRepository reviewRepository;
  private final ReviewImgRepository reviewImgRepository;
  private final ReviewMapper reviewMapper;
  private final AgentPerformanceService agentPerformanceService;
  private final MemberService memberService;

  private static final int MAX_IMAGE_COUNT = 3;

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
    ApplicationForm applicationForm = applicationFormService.findApplicationFormById(request.getApplicationFormId());
    validateReviewer(applicationForm, member);
    validateReviewable(applicationForm);

    // 이미지 파일 개수 검증
    validateImageCount(request.getReviewImgList(), 0, 0);

    try {
      Review review = Review.create(applicationForm, applicationForm.getClient(), applicationForm.getAgent(), request.getRating(), request.getComment());

      addReviewImages(request.getReviewImgList(), review);

      // 대리인 통계 업데이트
      agentPerformanceService.addReviewStats(review.getAgent(), request.getRating());

      return reviewRepository.save(review).getReviewId();
    } catch (Exception e) {
      log.error("리뷰 생성 중 오류: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }
  }

  @Transactional
  public void updateReview(UUID reviewId, ReviewEditRequest request, Member member) {
    Review review = findReviewById(reviewId);

    double oldRating = review.getRating();

    // 수정 권한 검증
    validateEditor(review, member);

    // 리뷰 수정 기간 검증
    ZonedDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1).atZone(ZoneId.of("Asia/Seoul"));
    if (review.getCreatedDate().isBefore(oneMonthAgo.toInstant())) {
      throw new CustomException(ErrorCode.REVIEW_EDIT_PERIOD_EXPIRED);
    }

    // 이미지 파일 개수 검증
    List<UUID> deleteIdList = request.getDeleteImgIdList();
    List<ReviewImg> imagesToDelete = new ArrayList<>();
    if (deleteIdList != null && !deleteIdList.isEmpty()) {
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

    // 대리인 통계 업데이트
    agentPerformanceService.updateReviewStats(review.getAgent(), oldRating, request.getRating());
  }

  @Transactional
  public void deleteReview(UUID reviewId, Member member) {
    Review review = findReviewById(reviewId);

    // 삭제 권한 검증
    validateEditor(review, member);

    // s3에서 이미지 삭제
    List<ReviewImg> imagesToDelete = review.getReviewImgList();
    deleteImages(imagesToDelete, review);

    // 대리인 통계 업데이트
    agentPerformanceService.deleteReviewStats(review.getAgent(), review.getRating());

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
      throw new CustomException(ErrorCode.NO_AUTH_TO_REVIEW_COMMENT);
    }
  }

  /**
   * 리뷰 작성자가 실제 신청서의 의뢰인인지 검증
   */
  private void validateReviewer(ApplicationForm applicationForm, Member member) {
    if (!applicationForm.getClient().getMemberId().equals(member.getMemberId())) {
      throw new CustomException(ErrorCode.NO_AUTH_TO_REVIEW);
    }
  }

  /**
   * 리뷰 수정/삭제 권한이 있는지 검증
   */
  private void validateEditor(Review review, Member member) {
    if (!review.getClient().getMemberId().equals(member.getMemberId())) {
      throw new CustomException(ErrorCode.NO_AUTH_TO_REVIEW);
    }
  }

  /**
   * 성공 여부, 중복 여부 검증
   */
  private void validateReviewable(ApplicationForm applicationForm) {
    // TODO: 성공 상태의 신청서가 아니면 리뷰 작성 불가
//    if (!applicationForm.getApplicationFormStatus().equals(ApplicationFormStatus.SUCCEEDED)) {
//      throw new CustomException(ErrorCode.CANNOT_REVIEW_NOT_SUCCEEDED_FORM);
//    }
    // 이미 해당 신청서에 대한 리뷰가 존재하는지 확인
    if (reviewRepository.existsByApplicationForm(applicationForm)) {
      throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
    }
  }

  /**
   * 이미지 파일 개수 검증
   */
  private void validateImageCount(List<MultipartFile> imageFileList, int currentFileCount, int deleteFileCount) {
    if (imageFileList != null && currentFileCount - deleteFileCount + imageFileList.size() > MAX_IMAGE_COUNT) {
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
      log.error("리뷰 이미지 업로드 중 오류: {}, 업로드된 {}개 파일 롤백.", e.getMessage(), storedPaths.size(), e);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }
    return storedPaths;
  }

  /**
   * 이미지 업로드 (S3, DB)
   */
  private void addReviewImages(List<MultipartFile> imageFileList, Review review) {
    if (imageFileList == null || imageFileList.isEmpty()) {
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
