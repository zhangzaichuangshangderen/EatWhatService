package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.FriendLinksMapper;
import com.tencent.wxcloudrun.dao.UsersMapper;
import com.tencent.wxcloudrun.dto.FriendLinkView;
import com.tencent.wxcloudrun.model.FriendLink;
import com.tencent.wxcloudrun.model.UserProfile;
import com.tencent.wxcloudrun.service.FriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {
  private static final String STATUS_PENDING = "pending";
  private static final String STATUS_CONFIRMED = "confirmed";
  private static final String STATUS_REJECTED = "rejected";

  private final FriendLinksMapper friendLinksMapper;
  private final UsersMapper usersMapper;

  public FriendLinkServiceImpl(@Autowired FriendLinksMapper friendLinksMapper,
                               @Autowired UsersMapper usersMapper) {
    this.friendLinksMapper = friendLinksMapper;
    this.usersMapper = usersMapper;
  }

  @Override
  @Transactional
  public FriendLinkView requestFriendAccess(String requesterUserId, String viewerUserId, String note) {
    if (requesterUserId == null || viewerUserId == null) {
      return null;
    }
    String requester = requesterUserId.trim();
    String viewer = viewerUserId.trim();
    if (requester.isEmpty() || viewer.isEmpty() || requester.equals(viewer)) {
      return null;
    }
    UserProfile friendProfile = usersMapper.findByUserId(viewer);
    if (friendProfile == null) {
      return null;
    }

    FriendLink existing = friendLinksMapper.findLatestByPair(requester, viewer);
    if (existing != null) {
      if (STATUS_CONFIRMED.equals(existing.getStatus())) {
        throw new IllegalStateException("你们已经是好友了！");
      }
      if (STATUS_PENDING.equals(existing.getStatus())) {
        throw new IllegalStateException("好友申请已发送，请等待对方确认");
      }
      if (STATUS_REJECTED.equals(existing.getStatus())) {
        friendLinksMapper.updateRequestAsPending(existing.getId(), viewer, normalizeNote(note));
        return toView(friendLinksMapper.findById(existing.getId()));
      }
    }
    FriendLink reverse = friendLinksMapper.findLatestByPair(viewer, requester);
    if (reverse != null) {
      if (STATUS_CONFIRMED.equals(reverse.getStatus())) {
        throw new IllegalStateException("你们已经是好友了！");
      }
      if (STATUS_PENDING.equals(reverse.getStatus())) {
        throw new IllegalStateException("对方已向你发起好友申请，请先同意对方申请");
      }
    }

    FriendLink link = new FriendLink();
    link.setRequesterUserId(requester);
    link.setViewerUserId(viewer);
    link.setStatus(STATUS_PENDING);
    link.setRequestNote(normalizeNote(note));
    friendLinksMapper.create(link);
    return toView(friendLinksMapper.findById(link.getId()));
  }

  @Override
  @Transactional
  public FriendLinkView decideRequest(String viewerUserId, Long linkId, boolean accept) {
    if (viewerUserId == null || linkId == null || linkId <= 0) {
      return null;
    }
    String status = accept ? STATUS_CONFIRMED : STATUS_REJECTED;
    int updated = friendLinksMapper.updateStatus(linkId, viewerUserId, status, LocalDateTime.now());
    if (updated <= 0) {
      return null;
    }
    return toView(friendLinksMapper.findById(linkId));
  }

  @Override
  public List<FriendLinkView> listIncomingRequests(String viewerUserId) {
    return mapViews(friendLinksMapper.listPendingByViewerUserId(viewerUserId));
  }

  @Override
  public List<FriendLinkView> listApprovedFriends(String viewerUserId) {
    List<FriendLink> allConfirmed = new ArrayList<FriendLink>();
    allConfirmed.addAll(friendLinksMapper.listConfirmedByViewerUserId(viewerUserId));
    allConfirmed.addAll(friendLinksMapper.listConfirmedByRequesterUserId(viewerUserId));
    Map<String, FriendLinkView> friendMap = new LinkedHashMap<String, FriendLinkView>();
    for (FriendLink link : allConfirmed) {
      String friendUserId = resolveFriendUserId(viewerUserId, link);
      if (friendUserId == null || friendUserId.isEmpty()) {
        continue;
      }
      FriendLinkView candidate = toView(link);
      FriendLinkView existing = friendMap.get(friendUserId);
      if (existing == null || isAfter(candidate.getConfirmAt(), existing.getConfirmAt())) {
        friendMap.put(friendUserId, candidate);
      }
    }
    return new ArrayList<FriendLinkView>(friendMap.values());
  }

  @Override
  public FriendLinkView getLatestOutgoingRequest(String requesterUserId) {
    return toView(friendLinksMapper.findLatestByRequesterUserId(requesterUserId));
  }

  @Override
  public boolean canViewFriendRecords(String viewerUserId, String requesterUserId) {
    if (viewerUserId == null || requesterUserId == null) {
      return false;
    }
    String viewer = viewerUserId.trim();
    String requester = requesterUserId.trim();
    if (viewer.isEmpty() || requester.isEmpty()) {
      return false;
    }
    return friendLinksMapper.countConfirmedBetween(viewer, requester) > 0;
  }

  @Override
  @Transactional
  public FriendLinkView acceptFriendCard(String viewerUserId, String requesterUserId) {
    if (viewerUserId == null || requesterUserId == null) {
      return null;
    }
    String viewer = viewerUserId.trim();
    String requester = requesterUserId.trim();
    if (viewer.isEmpty() || requester.isEmpty() || viewer.equals(requester)) {
      return null;
    }
    UserProfile requesterProfile = usersMapper.findByUserId(requester);
    if (requesterProfile == null) {
      return null;
    }
    FriendLink existing = friendLinksMapper.findLatestByPair(requester, viewer);
    if (existing != null) {
      if (STATUS_CONFIRMED.equals(existing.getStatus())) {
        return toView(existing);
      }
      int updated = friendLinksMapper.updateStatusFromAny(existing.getId(), viewer, STATUS_CONFIRMED, LocalDateTime.now());
      if (updated > 0) {
        return toView(friendLinksMapper.findById(existing.getId()));
      }
    }

    FriendLink link = new FriendLink();
    link.setRequesterUserId(requester);
    link.setViewerUserId(viewer);
    link.setStatus(STATUS_PENDING);
    link.setRequestNote("friend-card-auto-confirm");
    friendLinksMapper.create(link);
    int updated = friendLinksMapper.updateStatus(link.getId(), viewer, STATUS_CONFIRMED, LocalDateTime.now());
    if (updated <= 0) {
      return null;
    }
    return toView(friendLinksMapper.findById(link.getId()));
  }

  private List<FriendLinkView> mapViews(List<FriendLink> links) {
    List<FriendLinkView> views = new ArrayList<FriendLinkView>(links.size());
    for (FriendLink link : links) {
      views.add(toView(link));
    }
    return views;
  }

  private FriendLinkView toView(FriendLink link) {
    if (link == null) {
      return null;
    }
    FriendLinkView view = new FriendLinkView();
    view.setId(link.getId());
    view.setRequesterUserId(link.getRequesterUserId());
    view.setViewerUserId(link.getViewerUserId());
    view.setStatus(link.getStatus());
    view.setRequestNote(link.getRequestNote());
    view.setRequestAt(link.getRequestAt());
    view.setConfirmAt(link.getConfirmAt());
    UserProfile requesterProfile = usersMapper.findByUserId(link.getRequesterUserId());
    if (requesterProfile != null) {
      view.setRequesterNickName(requesterProfile.getNickName());
      view.setRequesterAvatarUrl(requesterProfile.getAvatarUrl());
    }
    UserProfile viewerProfile = usersMapper.findByUserId(link.getViewerUserId());
    if (viewerProfile != null) {
      view.setViewerNickName(viewerProfile.getNickName());
      view.setViewerAvatarUrl(viewerProfile.getAvatarUrl());
    }
    return view;
  }

  private String normalizeNote(String note) {
    if (note == null) {
      return null;
    }
    String trimmed = note.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
  }

  private String resolveFriendUserId(String currentUserId, FriendLink link) {
    if (link == null) {
      return null;
    }
    if (currentUserId.equals(link.getRequesterUserId())) {
      return link.getViewerUserId();
    }
    if (currentUserId.equals(link.getViewerUserId())) {
      return link.getRequesterUserId();
    }
    return null;
  }

  private boolean isAfter(LocalDateTime current, LocalDateTime previous) {
    if (current == null) {
      return false;
    }
    if (previous == null) {
      return true;
    }
    return current.isAfter(previous);
  }
}
