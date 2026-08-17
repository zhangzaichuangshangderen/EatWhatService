package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteLeaderboardItem {
  private Integer rank;
  private String inviterUserId;
  private String inviterNickName;
  private String inviterAvatarUrl;
  private Integer invitedCount;
  private LocalDateTime completedAt;
}
