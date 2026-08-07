package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.UserProfileUpsertRequest;
import com.tencent.wxcloudrun.service.UserProfileService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class UserProfileController {

  private final UserProfileService userProfileService;

  public UserProfileController(@Autowired UserProfileService userProfileService) {
    this.userProfileService = userProfileService;
  }

  @GetMapping(value = "/api/users/me")
  ApiResponse me(HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(userProfileService.findByUserId(userId.get()));
  }

  @PutMapping(value = "/api/users/me")
  ApiResponse upsert(@RequestBody(required = false) UserProfileUpsertRequest request,
                     HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    String validationError = validateRequest(request);
    if (validationError != null) {
      return ApiResponse.error(validationError);
    }
    return ApiResponse.ok(userProfileService.upsertProfile(userId.get(), request));
  }

  private String validateRequest(UserProfileUpsertRequest request) {
    if (request == null) {
      return null;
    }
    String nickName = trimToEmpty(request.getNickName());
    String avatarUrl = trimToEmpty(request.getAvatarUrl());
    if (!nickName.isEmpty() && nickName.length() > 64) {
      return "昵称长度不能超过64";
    }
    if (!avatarUrl.isEmpty() && avatarUrl.length() > 512) {
      return "头像链接过长";
    }
    return null;
  }

  private String trimToEmpty(String text) {
    return text == null ? "" : text.trim();
  }
}
