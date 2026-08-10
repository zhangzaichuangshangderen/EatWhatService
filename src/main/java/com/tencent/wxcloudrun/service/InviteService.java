package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.InviteProgressResponse;

public interface InviteService {
  void bindInviter(String inviteeUserId, String inviterUserId);

  void markInviteeQualified(String inviteeUserId);

  InviteProgressResponse getInviteProgress(String inviterUserId);
}
