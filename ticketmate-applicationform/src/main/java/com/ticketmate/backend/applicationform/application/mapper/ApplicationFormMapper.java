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
import java.util.List;

public interface ApplicationFormMapper {

  // List<HopeAreaRequest> -> List<HopeArea> (DTO 리스트 -> 엔티티 리스트)
  List<HopeArea> toHopeAreaList(List<HopeAreaRequest> hopeAreaRequestList);

  // HopeArea -> HopeAreaResponse (엔티티 -> DTO)
  HopeAreaResponse toHopeAreaResponse(HopeArea hopeArea);

  // List<HopeArea> -> List<HopeAreaResponse> (엔티티 리스트 -> DTO 리스트)
  List<HopeAreaResponse> toHopeAreaResponseList(List<HopeArea> hopeAreaList);

  // List<ApplicationFormDetail> -> List<ApplicationFormDetailResponse> (엔티티 리스트 -> DTO 리스트)
  List<ApplicationFormDetailResponse> toApplicationFormDetailResponseList(List<ApplicationFormDetail> applicationFormDetailList);

  ApplicationFormFilteredResponse toApplicationFormFilteredResponse(ApplicationFormFilteredInfo info);

  // RejectionReason -> RejectionReasonResponse (엔티티 -> DTO)
  RejectionReasonResponse toRejectionReasonResponse(RejectionReason rejectionReason);
}
