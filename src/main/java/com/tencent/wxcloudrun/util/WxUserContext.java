package com.tencent.wxcloudrun.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class WxUserContext {

  private WxUserContext() {
  }

  /**
   * 从微信云托管注入的请求头解析用户 openid。
   * 小程序需通过 wx.cloud.callContainer 调用，详见微信云托管文档。
   */
  public static Optional<String> resolveOpenId(HttpServletRequest request) {
    if (request == null) {
      return Optional.empty();
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
