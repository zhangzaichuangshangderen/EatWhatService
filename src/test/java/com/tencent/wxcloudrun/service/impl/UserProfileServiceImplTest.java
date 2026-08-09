package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.UserProfileUpsertRequest;
import com.tencent.wxcloudrun.model.UserProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileServiceImplTest {

  @Test
  void lookupDoesNotCreateUserBeforeProfileConsent() {
    UsersMapper mapper = mock(UsersMapper.class);
    UserProfileServiceImpl service = new UserProfileServiceImpl(mapper);
    when(mapper.findByUserId("user-a")).thenReturn(null);

    assertNull(service.findByUserId("user-a"));

    verify(mapper, never()).insertIgnore("user-a");
  }

  @Test
  void upsertCreatesUserAndPersistsAuthorizedProfile() {
    UsersMapper mapper = mock(UsersMapper.class);
    UserProfileServiceImpl service = new UserProfileServiceImpl(mapper);
    UserProfile stored = new UserProfile();
    stored.setUserId("user-a");
    when(mapper.findByUserId("user-a"))
      .thenReturn(null)
      .thenReturn(stored)
      .thenReturn(stored);
    UserProfileUpsertRequest request = new UserProfileUpsertRequest();
    request.setNickName("小饭团");
    request.setAvatarUrl("cloud://avatar.jpg");

    service.upsertProfile("user-a", request);

    verify(mapper).insertIgnore("user-a");
    verify(mapper).updateProfile(stored);
    assertEquals("小饭团", stored.getNickName());
    assertEquals("cloud://avatar.jpg", stored.getAvatarUrl());
  }
}
