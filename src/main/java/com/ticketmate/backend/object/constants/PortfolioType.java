package com.ticketmate.backend.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PortfolioType {
    UNDER_REVIEW("포트폴리오 검토중"),  // 현재 관리자가 검토중인 포트폴리오
    REVIEW_COMPLETED("승인된 포트폴리오"),  // 관리자가 승인해 의뢰인 -> 대리인으로 바뀐 포트폴리오
    COMPANION("반려된 포트폴리오");  // 관리자에게 반려당한 포트폴리오
    private final String description;
    }
