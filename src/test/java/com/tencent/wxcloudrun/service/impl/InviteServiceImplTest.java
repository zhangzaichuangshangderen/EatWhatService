package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.UserInvitesMapper;
import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.InviteLeaderboardResponse;
import com.tencent.wxcloudrun.dto.InviteLeaderboardRow;
import com.tencent.wxcloudrun.dto.InviteProgressResponse;
import com.tencent.wxcloudrun.model.UserInvite;
import com.tencent.wxcloudrun.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InviteServiceImplTest {

  private UserInvitesMapper userInvitesMapper;
  private UsersMapper usersMapper;
  private InviteServiceImpl service;

  @BeforeEach
  void setUp() {
    userInvitesMapper = mock(UserInvitesMapper.class);
    usersMapper = mock(UsersMapper.class);
    service = new InviteServiceImpl(userInvitesMapper, usersMapper);
  }

  @Test
  void progressUsesTargetTwentyAndCountsQualified() {
    UserInvite inviteA = new UserInvite();
    inviteA.setInviteeUserId("u-a");
    inviteA.setIsQualified(true);
    inviteA.setBindAt(LocalDateTime.now().minusDays(2));
    inviteA.setQualifiedAt(LocalDateTime.now().minusDays(1));

    UserInvite inviteB = new UserInvite();
    inviteB.setInviteeUserId("u-b");
    inviteB.setIsQualified(false);
    inviteB.setBindAt(LocalDateTime.now().minusHours(1));

    when(userInvitesMapper.listByInviterUserId("inviter-1")).thenReturn(Arrays.asList(inviteA, inviteB));

    InviteProgressResponse response = service.getInviteProgress("inviter-1");

    assertEquals(20, response.getTargetCount());
    assertEquals(2, response.getTotalInvited());
    assertEquals(1, response.getQualifiedCount());
    assertEquals(2, response.getItems().size());
  }

  @Test
  void leaderboardReturnsOrderedRowsWithRanksAndProfiles() {
    InviteLeaderboardRow rowA = new InviteLeaderboardRow();
    rowA.setInviterUserId("inviter-a");
    rowA.setInvitedCount(23);
    rowA.setCompletedAt(LocalDateTime.of(2026, 8, 10, 8, 0));

    InviteLeaderboardRow rowB = new InviteLeaderboardRow();
    rowB.setInviterUserId("inviter-b");
    rowB.setInvitedCount(20);
    rowB.setCompletedAt(LocalDateTime.of(2026, 8, 11, 9, 0));

    when(userInvitesMapper.listLeaderboardRows(eq(20), eq(19), eq(50)))
      .thenReturn(Arrays.asList(rowA, rowB));

    UserProfile inviterA = new UserProfile();
    inviterA.setUserId("inviter-a");
    inviterA.setNickName("甲");
    inviterA.setAvatarUrl("https://a.example/avatar.png");
    when(usersMapper.findByUserId("inviter-a")).thenReturn(inviterA);
    when(usersMapper.findByUserId("inviter-b")).thenReturn(null);

    InviteLeaderboardResponse response = service.getInviteLeaderboard(50);

    assertEquals(20, response.getTargetCount());
    assertEquals(50, response.getRewardLimit());
    assertEquals(2, response.getList().size());
    assertEquals(1, response.getList().get(0).getRank().intValue());
    assertEquals("甲", response.getList().get(0).getInviterNickName());
    assertEquals(2, response.getList().get(1).getRank().intValue());
    assertNotNull(response.getList().get(0).getCompletedAt());
  }

  @Test
  void leaderboardLimitIsSanitized() {
    when(userInvitesMapper.listLeaderboardRows(eq(20), eq(19), anyInt()))
      .thenReturn(Collections.emptyList());

    service.getInviteLeaderboard(-1);
    ArgumentCaptor<Integer> cap = ArgumentCaptor.forClass(Integer.class);
    verify(userInvitesMapper).listLeaderboardRows(eq(20), eq(19), cap.capture());
    assertEquals(50, cap.getValue().intValue());
  }
}
