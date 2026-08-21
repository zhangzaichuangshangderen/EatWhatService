package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.WxAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

  private final WxAuthService wxAuthService;

  public AuthController(@Autowired WxAuthService wxAuthService) {
    this.wxAuthService = wxAuthService;
  }

  /**
   * 小程序登录接口。
   * 前端先用 wx.login() 拿到 code，再将 {"code":"..."} POST 到本接口，
   * 后端用 code 向微信换取 openid 并签发 JWT；后续业务接口在
   * Authorization: Bearer &lt;token&gt; 中携带该 token。
   */
  @PostMapping("/api/auth/login")
  ApiResponse login(@RequestBody(required = false) Map<String, String> body) {
    if (body == null || body.get("code") == null || body.get("code").trim().isEmpty()) {
      return ApiResponse.error("缺少 login code");
    }
    try {
      String openid = wxAuthService.code2Session(body.get("code").trim());
      String token = wxAuthService.issueToken(openid);
      Map<String, String> data = new HashMap<>();
      data.put("token", token);
      data.put("openid", openid);
      return ApiResponse.ok(data);
    } catch (Exception e) {
      return ApiResponse.error("登录失败：" + e.getMessage());
    }
  }
}
