package com.ticketmate.backend.member.core.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ticketmate.backend.common.core.constant.SortField;
import com.ticketmate.backend.common.core.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberFollowSortField implements SortField {

  CREATED_DATE("createdDate"),

  FOLLOWER_COUNT("followerCount");

  private final String property;

  @JsonCreator
  public static MemberFollowSortField from(String value) {
    return CommonUtil.stringToSortField(MemberFollowSortField.class, value);
  }
}
