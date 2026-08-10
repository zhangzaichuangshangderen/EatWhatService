package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteProgressItem {
  private String inviteeUserId;
  private String inviteeNickName;
  private String inviteeAvatarUrl;
  private Boolean qualified;
  private LocalDateTime bindAt;
  private LocalDateTime qualifiedAt;
}
