package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class UserProfileUpsertRequest {

  private String nickName;

  private String avatarUrl;
}
