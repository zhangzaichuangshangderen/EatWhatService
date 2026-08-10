package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

@Data
public class InviteProgressResponse {
  private Integer totalInvited;
  private Integer qualifiedCount;
  private Integer targetCount;
  private List<InviteProgressItem> items;
}
