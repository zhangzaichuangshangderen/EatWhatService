package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInvite {
  private Long id;
  private String inviterUserId;
  private String inviteeUserId;
  private LocalDateTime bindAt;
  private Boolean isQualified;
  private LocalDateTime qualifiedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
