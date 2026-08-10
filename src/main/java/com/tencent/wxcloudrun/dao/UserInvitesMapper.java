package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.UserInvite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserInvitesMapper {
  int create(UserInvite invite);

  UserInvite findByInviteeUserId(@Param("inviteeUserId") String inviteeUserId);

  List<UserInvite> listByInviterUserId(@Param("inviterUserId") String inviterUserId);

  int markQualified(@Param("inviteeUserId") String inviteeUserId);
}
