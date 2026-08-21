package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.service.WxAuthService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一登录态拦截器：从 Authorization: Bearer &lt;token&gt; 解析 JWT，
 * 校验通过后将 openid 写入 request 属性，供 WxUserContext 读取。
 * 非法/过期的 token 直接返回 401；不带 token 的请求放行，交由控制器按原逻辑处理
 * （含云托管 X-WX-OPENID 头兼容，便于迁移过渡期双跑）。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final WxAuthService wxAuthService;

  public AuthInterceptor(@Autowired WxAuthService wxAuthService) {
    this.wxAuthService = wxAuthService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    throws IOException {
    String path = request.getRequestURI();
    if (isWhitelisted(path)) {
      return true;
    }
    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7).trim();
      try {
        String openid = wxAuthService.validateToken(token);
        request.setAttribute(WxUserContext.OPENID_ATTRIBUTE, openid);
        return true;
      } catch (Exception e) {
        writeUnauthorized(response, "登录已失效，请重新登录");
        return false;
      }
    }
    // 无 token：放行，由控制器继续处理（依赖 X-WX-OPENID 头兼容或返回未登录）
    return true;
  }

  private boolean isWhitelisted(String path) {
    if (path.startsWith("/api/auth/login")) {
      return true;
    }
    // 非 /api 资源（首页、静态文件等）公开访问
    return !path.startsWith("/api/");
  }

  private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write("{\"code\":-1,\"errorMsg\":\"" + msg + "\"}");
  }
}
