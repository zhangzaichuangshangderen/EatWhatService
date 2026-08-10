package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class FriendLinkRequest {
  private String friendUserId;
  private String requestNote;
}
