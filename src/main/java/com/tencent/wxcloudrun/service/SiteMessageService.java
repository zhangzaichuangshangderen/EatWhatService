package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.SiteMessage;

import java.util.List;

public interface SiteMessageService {
  List<SiteMessage> listMessages(String userId, int limit);

  int unreadCount(String userId);

  boolean markRead(String userId, Long messageId);

  void ensureWelcomeMessage(String userId);
}
