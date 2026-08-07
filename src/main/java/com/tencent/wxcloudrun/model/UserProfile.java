package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserProfile implements Serializable {

  private String userId;

  private String nickName;

  private String avatarUrl;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
