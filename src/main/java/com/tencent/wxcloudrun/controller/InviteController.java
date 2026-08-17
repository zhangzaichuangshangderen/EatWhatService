package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.InviteBindRequest;
import com.tencent.wxcloudrun.dto.InviteCleanupRequest;
import com.tencent.wxcloudrun.service.InviteService;
import com.tencent.wxcloudrun.service.UserProfileService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  private final String inviteCleanupAdminOpenIds;

  public InviteController(@Autowired InviteService inviteService,
                          @Autowired UserProfileService userProfileService,
                          @Value("${INVITE_CLEANUP_ADMIN_OPENIDS:}") String inviteCleanupAdminOpenIds) {
    this.inviteService = inviteService;
    this.userProfileService = userProfileService;
    this.inviteCleanupAdminOpenIds = inviteCleanupAdminOpenIds;
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

  @PostMapping(value = "/api/invites/cleanup-dirty")
  ApiResponse cleanupDirty(@RequestBody(required = false) InviteCleanupRequest request,
                           HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (!isCleanupAdmin(userId.get())) {
      return ApiResponse.error("无权限执行数据清洗");
    }
    boolean dryRun = request == null || request.getDryRun() == null || request.getDryRun().booleanValue();
    boolean syncQualifiedFromDietRecords = request == null
      || request.getSyncQualifiedFromDietRecords() == null
      || request.getSyncQualifiedFromDietRecords().booleanValue();
    return ApiResponse.ok(inviteService.cleanupInviteDirtyData(dryRun, syncQualifiedFromDietRecords));
  }

  private boolean isCleanupAdmin(String userId) {
    String currentUserId = userId == null ? "" : userId.trim();
    if (currentUserId.isEmpty()) {
      return false;
    }
    String allowList = inviteCleanupAdminOpenIds == null ? "" : inviteCleanupAdminOpenIds.trim();
    if (allowList.isEmpty()) {
      return false;
    }
    String[] entries = allowList.split(",");
    for (String entry : entries) {
      if (currentUserId.equals(entry == null ? "" : entry.trim())) {
        return true;
      }
    }
    return false;
  }
}
