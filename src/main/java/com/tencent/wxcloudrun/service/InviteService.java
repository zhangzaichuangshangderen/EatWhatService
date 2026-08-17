package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.InviteProgressResponse;
import com.tencent.wxcloudrun.dto.InviteLeaderboardResponse;

public interface InviteService {
  void bindInviter(String inviteeUserId, String inviterUserId);

  void markInviteeQualified(String inviteeUserId);

  InviteProgressResponse getInviteProgress(String inviterUserId);

  InviteLeaderboardResponse getInviteLeaderboard(int limit);
}
