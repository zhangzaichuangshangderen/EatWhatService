package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.FriendLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FriendLinksMapper {
  int create(FriendLink link);

  FriendLink findById(@Param("id") Long id);

  FriendLink findLatestByRequesterUserId(@Param("requesterUserId") String requesterUserId);

  FriendLink findLatestByPair(@Param("requesterUserId") String requesterUserId,
                              @Param("viewerUserId") String viewerUserId);

  List<FriendLink> listPendingByViewerUserId(@Param("viewerUserId") String viewerUserId);

  List<FriendLink> listConfirmedByViewerUserId(@Param("viewerUserId") String viewerUserId);

  List<FriendLink> listConfirmedByRequesterUserId(@Param("requesterUserId") String requesterUserId);

  int countConfirmedBetween(@Param("userIdA") String userIdA,
                            @Param("userIdB") String userIdB);

  int updateStatus(@Param("id") Long id,
                   @Param("viewerUserId") String viewerUserId,
                   @Param("status") String status,
                   @Param("confirmAt") LocalDateTime confirmAt);

  int updateStatusFromAny(@Param("id") Long id,
                          @Param("viewerUserId") String viewerUserId,
                          @Param("status") String status,
                          @Param("confirmAt") LocalDateTime confirmAt);

  int updateRequestAsPending(@Param("id") Long id,
                             @Param("viewerUserId") String viewerUserId,
                             @Param("requestNote") String requestNote);
}
