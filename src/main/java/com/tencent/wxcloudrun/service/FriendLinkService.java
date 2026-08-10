package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.FriendLinkView;

import java.util.List;

public interface FriendLinkService {
  FriendLinkView requestFriendAccess(String requesterUserId, String viewerUserId, String note);

  FriendLinkView decideRequest(String viewerUserId, Long linkId, boolean accept);

  List<FriendLinkView> listIncomingRequests(String viewerUserId);

  List<FriendLinkView> listApprovedFriends(String viewerUserId);

  FriendLinkView getLatestOutgoingRequest(String requesterUserId);

  boolean canViewFriendRecords(String viewerUserId, String requesterUserId);

  FriendLinkView acceptFriendCard(String viewerUserId, String requesterUserId);
}
