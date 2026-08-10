package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SiteMessage {
  private Long id;
  private String userId;
  private String title;
  private String content;
  private String type;
  private Boolean isRead;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
