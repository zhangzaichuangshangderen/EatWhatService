package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UsersMapper {

  UserProfile findByUserId(@Param("userId") String userId);

  int insertIgnore(@Param("userId") String userId);

  int updateProfile(UserProfile profile);

  int countDirtyInviteNickNames();

  int cleanDirtyInviteNickNames();
}
