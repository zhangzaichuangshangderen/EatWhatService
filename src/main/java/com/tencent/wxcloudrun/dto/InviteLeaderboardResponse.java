package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

@Data
public class InviteLeaderboardResponse {
  private Integer targetCount;
  private Integer rewardLimit;
  private List<InviteLeaderboardItem> list;
}
