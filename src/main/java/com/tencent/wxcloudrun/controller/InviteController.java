package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.InviteBindRequest;
import com.tencent.wxcloudrun.service.InviteService;
import com.tencent.wxcloudrun.service.UserProfileService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class InviteController {
  private final InviteService inviteService;
  private final UserProfileService userProfileService;

  public InviteController(@Autowired InviteService inviteService,
                          @Autowired UserProfileService userProfileService) {
    this.inviteService = inviteService;
    this.userProfileService = userProfileService;
  }

  @PostMapping(value = "/api/invites/bind")
  ApiResponse bind(@RequestBody(required = false) InviteBindRequest request, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (request == null || request.getInviterUserId() == null || request.getInviterUserId().trim().isEmpty()) {
      return ApiResponse.error("邀请人不能为空");
    }
    userProfileService.getOrCreate(userId.get());
    inviteService.bindInviter(userId.get(), request.getInviterUserId());
    return ApiResponse.ok();
  }

  @GetMapping(value = "/api/invites/progress")
  ApiResponse progress(HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(inviteService.getInviteProgress(userId.get()));
  }

  @GetMapping(value = "/api/invites/leaderboard")
  ApiResponse leaderboard(@RequestParam(value = "limit", required = false) Integer limit,
                          HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    int safeLimit = limit == null ? 50 : limit.intValue();
    return ApiResponse.ok(inviteService.getInviteLeaderboard(safeLimit));
  }
}
