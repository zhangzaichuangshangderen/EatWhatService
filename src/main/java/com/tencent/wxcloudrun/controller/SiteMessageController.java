package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.SiteMessageService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class SiteMessageController {
  private final SiteMessageService siteMessageService;

  public SiteMessageController(@Autowired SiteMessageService siteMessageService) {
    this.siteMessageService = siteMessageService;
  }

  @GetMapping(value = "/api/messages")
  ApiResponse list(@RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit,
                   HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    int safeLimit = limit == null ? 30 : limit;
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("items", siteMessageService.listMessages(userId.get(), safeLimit));
    data.put("unreadCount", siteMessageService.unreadCount(userId.get()));
    return ApiResponse.ok(data);
  }

  @PutMapping(value = "/api/messages/{id}/read")
  ApiResponse markRead(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    boolean updated = siteMessageService.markRead(userId.get(), id);
    if (!updated) {
      return ApiResponse.error("消息不存在或无权限");
    }
    return ApiResponse.ok();
  }
}
