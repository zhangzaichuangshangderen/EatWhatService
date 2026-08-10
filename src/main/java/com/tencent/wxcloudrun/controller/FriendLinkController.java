package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.FriendCardAcceptRequest;
import com.tencent.wxcloudrun.dto.FriendLinkDecisionRequest;
import com.tencent.wxcloudrun.dto.FriendLinkRequest;
import com.tencent.wxcloudrun.dto.FriendLinkView;
import com.tencent.wxcloudrun.service.DietRecordService;
import com.tencent.wxcloudrun.service.FriendLinkService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;

@RestController
public class FriendLinkController {
  private final FriendLinkService friendLinkService;
  private final DietRecordService dietRecordService;

  public FriendLinkController(@Autowired FriendLinkService friendLinkService,
                              @Autowired DietRecordService dietRecordService) {
    this.friendLinkService = friendLinkService;
    this.dietRecordService = dietRecordService;
  }

  @PostMapping(value = "/api/friends/request")
  ApiResponse requestFriend(@RequestBody(required = false) FriendLinkRequest request,
                            HttpServletRequest httpRequest) {
    Optional<String> requesterUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!requesterUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (request == null || request.getFriendUserId() == null || request.getFriendUserId().trim().isEmpty()) {
      return ApiResponse.error("好友ID不能为空");
    }
    try {
      FriendLinkView view = friendLinkService.requestFriendAccess(
        requesterUserId.get(),
        request.getFriendUserId(),
        request.getRequestNote()
      );
      if (view == null) {
        return ApiResponse.error("好友申请失败，请确认好友ID是否正确");
      }
      return ApiResponse.ok(view);
    } catch (IllegalStateException exception) {
      return ApiResponse.error(exception.getMessage());
    }
  }

  @PostMapping(value = "/api/friends/{id}/decision")
  ApiResponse decideRequest(@PathVariable("id") Long id,
                            @RequestBody(required = false) FriendLinkDecisionRequest request,
                            HttpServletRequest httpRequest) {
    Optional<String> viewerUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!viewerUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    boolean accept = request == null || request.getAccept() == null || request.getAccept();
    FriendLinkView updated = friendLinkService.decideRequest(viewerUserId.get(), id, accept);
    if (updated == null) {
      return ApiResponse.error("处理失败，记录不存在或状态已变更");
    }
    return ApiResponse.ok(updated);
  }

  @PostMapping(value = "/api/friends/card/accept")
  ApiResponse acceptByFriendCard(@RequestBody(required = false) FriendCardAcceptRequest request,
                                 HttpServletRequest httpRequest) {
    Optional<String> viewerUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!viewerUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (request == null || request.getRequesterUserId() == null || request.getRequesterUserId().trim().isEmpty()) {
      return ApiResponse.error("请求用户ID不能为空");
    }
    FriendLinkView view = friendLinkService.acceptFriendCard(viewerUserId.get(), request.getRequesterUserId());
    if (view == null) {
      return ApiResponse.error("添加好友失败，请稍后重试");
    }
    return ApiResponse.ok(view);
  }

  @GetMapping(value = "/api/friends/incoming")
  ApiResponse incoming(HttpServletRequest httpRequest) {
    Optional<String> viewerUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!viewerUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(friendLinkService.listIncomingRequests(viewerUserId.get()));
  }

  @GetMapping(value = "/api/friends/list")
  ApiResponse friends(HttpServletRequest httpRequest) {
    Optional<String> viewerUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!viewerUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(friendLinkService.listApprovedFriends(viewerUserId.get()));
  }

  @GetMapping(value = "/api/friends/me")
  ApiResponse myLatestRequest(HttpServletRequest httpRequest) {
    Optional<String> requesterUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!requesterUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(friendLinkService.getLatestOutgoingRequest(requesterUserId.get()));
  }

  @GetMapping(value = "/api/friends/{friendUserId}/diet-records/{date}")
  ApiResponse friendDay(@PathVariable("friendUserId") String friendUserId,
                        @PathVariable("date") String date,
                        HttpServletRequest httpRequest) {
    Optional<String> viewerUserId = WxUserContext.resolveOpenId(httpRequest);
    if (!viewerUserId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (!friendLinkService.canViewFriendRecords(viewerUserId.get(), friendUserId)) {
      return ApiResponse.error("无权限查看该好友记录");
    }
    LocalDate parsedDate = parseDate(date);
    if (parsedDate == null) {
      return ApiResponse.error("日期格式错误，请使用yyyy-MM-dd");
    }
    return ApiResponse.ok(dietRecordService.getDay(friendUserId, parsedDate));
  }

  private LocalDate parseDate(String date) {
    try {
      return LocalDate.parse(date);
    } catch (DateTimeException | NullPointerException exception) {
      return null;
    }
  }
}
