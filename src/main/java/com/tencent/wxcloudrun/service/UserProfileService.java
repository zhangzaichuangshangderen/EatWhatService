package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.UserProfileUpsertRequest;
import com.tencent.wxcloudrun.model.UserProfile;

public interface UserProfileService {

  UserProfile getOrCreate(String userId);

  UserProfile upsertProfile(String userId, UserProfileUpsertRequest request);
}
