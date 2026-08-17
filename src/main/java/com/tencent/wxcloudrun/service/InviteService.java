package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.InviteProgressResponse;
import com.tencent.wxcloudrun.dto.InviteLeaderboardResponse;
import com.tencent.wxcloudrun.dto.InviteCleanupResponse;

public interface InviteService {
  void bindInviter(String inviteeUserId, String inviterUserId);

  void markInviteeQualified(String inviteeUserId);

  InviteProgressResponse getInviteProgress(String inviterUserId);

  InviteLeaderboardResponse getInviteLeaderboard(int limit);

  InviteCleanupResponse cleanupInviteDirtyData(boolean dryRun, boolean syncQualifiedFromDietRecords);
}
