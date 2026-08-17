package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UserInvitesMapper;
import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.InviteLeaderboardItem;
import com.tencent.wxcloudrun.dto.InviteLeaderboardResponse;
import com.tencent.wxcloudrun.dto.InviteLeaderboardRow;
import com.tencent.wxcloudrun.dto.InviteProgressItem;
import com.tencent.wxcloudrun.dto.InviteProgressResponse;
import com.tencent.wxcloudrun.model.UserInvite;
import com.tencent.wxcloudrun.model.UserProfile;
import com.tencent.wxcloudrun.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InviteServiceImpl implements InviteService {
  private static final int REWARD_TARGET = 20;
  private static final int REWARD_LIMIT = 50;
  private static final int MAX_LEADERBOARD_LIMIT = 200;

  private final UserInvitesMapper userInvitesMapper;
  private final UsersMapper usersMapper;

  public InviteServiceImpl(@Autowired UserInvitesMapper userInvitesMapper,
                           @Autowired UsersMapper usersMapper) {
    this.userInvitesMapper = userInvitesMapper;
    this.usersMapper = usersMapper;
  }

  @Override
  @Transactional
  public void bindInviter(String inviteeUserId, String inviterUserId) {
    if (inviteeUserId == null || inviterUserId == null) {
      return;
    }
    String invitee = inviteeUserId.trim();
    String inviter = inviterUserId.trim();
    if (invitee.isEmpty() || inviter.isEmpty() || invitee.equals(inviter)) {
      return;
    }
    if (userInvitesMapper.findByInviteeUserId(invitee) != null) {
      return;
    }
    if (usersMapper.findByUserId(inviter) == null) {
      return;
    }
    UserInvite invite = new UserInvite();
    invite.setInviterUserId(inviter);
    invite.setInviteeUserId(invitee);
    invite.setIsQualified(false);
    userInvitesMapper.create(invite);
  }

  @Override
  @Transactional
  public void markInviteeQualified(String inviteeUserId) {
    if (inviteeUserId == null || inviteeUserId.trim().isEmpty()) {
      return;
    }
    userInvitesMapper.markQualified(inviteeUserId.trim());
  }

  @Override
  public InviteProgressResponse getInviteProgress(String inviterUserId) {
    List<UserInvite> invites = userInvitesMapper.listByInviterUserId(inviterUserId);
    List<InviteProgressItem> items = new ArrayList<InviteProgressItem>(invites.size());
    int qualifiedCount = 0;
    for (UserInvite invite : invites) {
      InviteProgressItem item = new InviteProgressItem();
      item.setInviteeUserId(invite.getInviteeUserId());
      UserProfile inviteeProfile = usersMapper.findByUserId(invite.getInviteeUserId());
      if (inviteeProfile != null) {
        item.setInviteeNickName(inviteeProfile.getNickName());
        item.setInviteeAvatarUrl(inviteeProfile.getAvatarUrl());
      }
      boolean qualified = Boolean.TRUE.equals(invite.getIsQualified());
      item.setQualified(qualified);
      item.setBindAt(invite.getBindAt());
      item.setQualifiedAt(invite.getQualifiedAt());
      items.add(item);
      if (qualified) {
        qualifiedCount += 1;
      }
    }
    InviteProgressResponse response = new InviteProgressResponse();
    response.setTotalInvited(invites.size());
    response.setQualifiedCount(qualifiedCount);
    response.setTargetCount(REWARD_TARGET);
    response.setItems(items);
    return response;
  }

  @Override
  public InviteLeaderboardResponse getInviteLeaderboard(int limit) {
    int safeLimit = limit <= 0 ? REWARD_LIMIT : Math.min(limit, MAX_LEADERBOARD_LIMIT);
    List<InviteLeaderboardRow> rows = userInvitesMapper.listLeaderboardRows(REWARD_TARGET, REWARD_TARGET - 1, safeLimit);
    List<InviteLeaderboardItem> items = new ArrayList<InviteLeaderboardItem>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
      InviteLeaderboardRow row = rows.get(i);
      InviteLeaderboardItem item = new InviteLeaderboardItem();
      item.setRank(i + 1);
      item.setInviterUserId(row.getInviterUserId());
      item.setInvitedCount(row.getInvitedCount());
      item.setCompletedAt(row.getCompletedAt());
      UserProfile inviterProfile = usersMapper.findByUserId(row.getInviterUserId());
      if (inviterProfile != null) {
        item.setInviterNickName(inviterProfile.getNickName());
        item.setInviterAvatarUrl(inviterProfile.getAvatarUrl());
      }
      items.add(item);
    }
    InviteLeaderboardResponse response = new InviteLeaderboardResponse();
    response.setTargetCount(REWARD_TARGET);
    response.setRewardLimit(REWARD_LIMIT);
    response.setList(items);
    return response;
  }
}
