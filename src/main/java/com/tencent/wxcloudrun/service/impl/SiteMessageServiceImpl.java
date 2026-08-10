package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.SiteMessagesMapper;
import com.tencent.wxcloudrun.model.SiteMessage;
import com.tencent.wxcloudrun.service.SiteMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteMessageServiceImpl implements SiteMessageService {
  private static final String WELCOME_MESSAGE_TYPE = "welcome";
  private static final String WELCOME_TITLE = "欢迎来到 FitFit 认真吃饭";
  private static final String WELCOME_CONTENT = "欢迎你加入 FitFit 认真吃饭！这里可以帮你完成基础代谢测算、每餐科学配餐和每日饮食记录。愿你越来越 fit、越来越健康，心愿达成。";

  private final SiteMessagesMapper siteMessagesMapper;

  public SiteMessageServiceImpl(@Autowired SiteMessagesMapper siteMessagesMapper) {
    this.siteMessagesMapper = siteMessagesMapper;
  }

  @Override
  public List<SiteMessage> listMessages(String userId, int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 100));
    return siteMessagesMapper.listByUserId(userId, safeLimit);
  }

  @Override
  public int unreadCount(String userId) {
    return siteMessagesMapper.countUnread(userId);
  }

  @Override
  public boolean markRead(String userId, Long messageId) {
    if (messageId == null || messageId <= 0) {
      return false;
    }
    return siteMessagesMapper.markRead(messageId, userId) > 0;
  }

  @Override
  public void ensureWelcomeMessage(String userId) {
    int count = siteMessagesMapper.countByUserAndType(userId, WELCOME_MESSAGE_TYPE);
    if (count > 0) {
      return;
    }
    SiteMessage message = new SiteMessage();
    message.setUserId(userId);
    message.setTitle(WELCOME_TITLE);
    message.setContent(WELCOME_CONTENT);
    message.setType(WELCOME_MESSAGE_TYPE);
    message.setIsRead(false);
    siteMessagesMapper.create(message);
  }
}
