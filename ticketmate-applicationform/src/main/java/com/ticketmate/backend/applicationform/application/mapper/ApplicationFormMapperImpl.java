package com.ticketmate.backend.applicationform.application.mapper;

import com.ticketmate.backend.applicationform.application.dto.request.HopeAreaRequest;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormDetailResponse;
import com.ticketmate.backend.applicationform.application.dto.response.ApplicationFormFilteredResponse;
import com.ticketmate.backend.applicationform.application.dto.response.HopeAreaResponse;
import com.ticketmate.backend.applicationform.application.dto.response.RejectionReasonResponse;
import com.ticketmate.backend.applicationform.application.dto.view.ApplicationFormFilteredInfo;
import com.ticketmate.backend.applicationform.infrastructure.entity.ApplicationFormDetail;
import com.ticketmate.backend.applicationform.infrastructure.entity.HopeArea;
import com.ticketmate.backend.applicationform.infrastructure.entity.RejectionReason;
import com.ticketmate.backend.storage.core.service.StorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationFormMapperImpl implements ApplicationFormMapper {

  private final ApplicationFormMapStruct mapStruct;
  private final StorageService storageService;

  @Override
  public List<HopeArea> toHopeAreaList(List<HopeAreaRequest> hopeAreaRequestList) {
    return mapStruct.toHopeAreaList(hopeAreaRequestList);
  }

  @Override
  public HopeAreaResponse toHopeAreaResponse(HopeArea hopeArea) {
    return mapStruct.toHopeAreaResponse(hopeArea);
  }

  @Override
  public List<HopeAreaResponse> toHopeAreaResponseList(List<HopeArea> hopeAreaList) {
    return mapStruct.toHopeAreaResponseList(hopeAreaList);
  }

  @Override
  public ApplicationFormDetailResponse toApplicationFormDetailResponse(ApplicationFormDetail applicationFormDetail) {
    return mapStruct.toApplicationFormDetailResponse(applicationFormDetail);
  }

  @Override
  public List<ApplicationFormDetailResponse> toApplicationFormDetailResponseList(List<ApplicationFormDetail> applicationFormDetailList) {
    return mapStruct.toApplicationFormDetailResponseList(applicationFormDetailList);
  }

  @Override
  public ApplicationFormFilteredResponse toApplicationFormFilteredResponse(ApplicationFormFilteredInfo info) {
    return new ApplicationFormFilteredResponse(
        info.applicationFormId(),
        info.concertName(),
        storageService.generatePublicUrl(info.concertThumbnailStoredPath()),
        info.agentNickname(),
        info.clientNickname(),
        info.submittedDate(),
        info.applicationFormStatus(),
        info.ticketOpenType()
    );
  }

  @Override
  public RejectionReasonResponse toRejectionReasonResponse(RejectionReason rejectionReason) {
    return mapStruct.toRejectionReasonResponse(rejectionReason);
  }
}
