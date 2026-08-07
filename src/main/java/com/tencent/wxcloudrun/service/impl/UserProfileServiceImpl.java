package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.UserProfileUpsertRequest;
import com.tencent.wxcloudrun.model.UserProfile;
import com.tencent.wxcloudrun.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private final UsersMapper usersMapper;

  public UserProfileServiceImpl(@Autowired UsersMapper usersMapper) {
    this.usersMapper = usersMapper;
  }

  @Override
  public UserProfile findByUserId(String userId) {
    return usersMapper.findByUserId(userId);
  }

  @Override
  @Transactional
  public UserProfile getOrCreate(String userId) {
    UserProfile existing = usersMapper.findByUserId(userId);
    if (existing != null) {
      return existing;
    }
    usersMapper.insertIgnore(userId);
    return usersMapper.findByUserId(userId);
  }

  @Override
  @Transactional
  public UserProfile upsertProfile(String userId, UserProfileUpsertRequest request) {
    UserProfile profile = getOrCreate(userId);
    if (request == null) {
      return profile;
    }
    String nickName = normalize(request.getNickName());
    String avatarUrl = normalize(request.getAvatarUrl());
    if (nickName == null && avatarUrl == null) {
      return profile;
    }
    profile.setNickName(nickName);
    profile.setAvatarUrl(avatarUrl);
    usersMapper.updateProfile(profile);
    return usersMapper.findByUserId(userId);
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
