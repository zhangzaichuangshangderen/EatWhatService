package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendLink {
  private Long id;
  private String requesterUserId;
  private String viewerUserId;
  private String status;
  private String requestNote;
  private LocalDateTime requestAt;
  private LocalDateTime confirmAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
