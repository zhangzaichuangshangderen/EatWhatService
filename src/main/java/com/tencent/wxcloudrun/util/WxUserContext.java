package com.tencent.wxcloudrun.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class WxUserContext {

  /**
   * 由 AuthInterceptor 从 JWT 解析后写入 request 的属性名。
   */
  public static final String OPENID_ATTRIBUTE = "openid";

  private WxUserContext() {
  }

  /**
   * 解析当前请求的用户 openid。解析优先级：
   *   1. AuthInterceptor 从 JWT 解析并放入 request attribute（自建/云服务部署的标准方式）；
   *   2. 兼容云托管通过 wx.cloud.callContainer 自动注入的 X-WX-OPENID / X-WX-FROM-OPENID 请求头（迁移过渡期）。
   * 离开云托管后，必须走第 1 种方式，由小程序在 Authorization: Bearer &lt;token&gt; 中携带登录态。
   */
  public static Optional<String> resolveOpenId(HttpServletRequest request) {
    if (request == null) {
      return Optional.empty();
    }
    Object attr = request.getAttribute(OPENID_ATTRIBUTE);
    if (attr instanceof String && !((String) attr).trim().isEmpty()) {
      return Optional.of(((String) attr).trim());
    }
    String openId = firstNonBlank(
      request.getHeader("X-WX-OPENID"),
      request.getHeader("X-WX-FROM-OPENID")
    );
    if (openId == null) {
      return Optional.empty();
    }
    openId = openId.trim();
    return openId.isEmpty() ? Optional.empty() : Optional.of(openId);
  }

  private static String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.trim().isEmpty()) {
        return value;
      }
    }
    return null;
  }
}
