package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendLinkView {
  private Long id;
  private String requesterUserId;
  private String requesterNickName;
  private String requesterAvatarUrl;
  private String viewerUserId;
  private String viewerNickName;
  private String viewerAvatarUrl;
  private String status;
  private String requestNote;
  private LocalDateTime requestAt;
  private LocalDateTime confirmAt;
}
