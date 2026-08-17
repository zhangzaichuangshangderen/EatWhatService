package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InviteLeaderboardRow {
  private String inviterUserId;
  private Integer invitedCount;
  private LocalDateTime completedAt;
}
