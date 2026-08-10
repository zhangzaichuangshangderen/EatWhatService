package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.UserProfileUpsertRequest;
import com.tencent.wxcloudrun.model.UserProfile;
import com.tencent.wxcloudrun.service.UserProfileService;
import com.tencent.wxcloudrun.service.SiteMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

  private final UsersMapper usersMapper;
  private final SiteMessageService siteMessageService;

  public UserProfileServiceImpl(@Autowired UsersMapper usersMapper,
                                @Autowired SiteMessageService siteMessageService) {
    this.usersMapper = usersMapper;
    this.siteMessageService = siteMessageService;
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
      siteMessageService.ensureWelcomeMessage(userId);
      return existing;
    }
    usersMapper.insertIgnore(userId);
    UserProfile created = usersMapper.findByUserId(userId);
    siteMessageService.ensureWelcomeMessage(userId);
    return created;
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
