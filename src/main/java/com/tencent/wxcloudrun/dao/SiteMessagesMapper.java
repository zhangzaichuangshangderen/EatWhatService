package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.SiteMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SiteMessagesMapper {
  int create(SiteMessage message);

  SiteMessage findById(@Param("id") Long id);

  List<SiteMessage> listByUserId(@Param("userId") String userId, @Param("limit") int limit);

  int markRead(@Param("id") Long id, @Param("userId") String userId);

  int countUnread(@Param("userId") String userId);

  int countByUserAndType(@Param("userId") String userId, @Param("type") String type);
}
