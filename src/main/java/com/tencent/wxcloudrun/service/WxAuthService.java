package com.tencent.wxcloudrun.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

/**
 * 微信登录与 Token 服务。
 *
 * 迁移到自建云服务后，云托管不再自动注入 openid，需由小程序 wx.login 拿到 code，
 * 经本服务 code2Session 换取 openid，并签发 JWT 作为后续请求的登录态。
 */
@Service
public class WxAuthService {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${wx.appid:}")
  private String appid;

  @Value("${wx.secret:}")
  private String secret;

  @Value("${jwt.secret:change-me-in-production}")
  private String jwtSecret;

  @Value("${jwt.expire-hours:720}")
  private long expireHours;

  /**
   * 用小程序 wx.login 返回的 code 调用微信 code2session 换取 openid。
   */
  public String code2Session(String code) {
    if (appid.isEmpty() || secret.isEmpty()) {
      throw new IllegalStateException("微信小程序 appid/secret 尚未配置");
    }
    String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
      + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
    Map<String, Object> resp;
    try {
      resp = restTemplate.getForObject(url, Map.class);
    } catch (Exception e) {
      throw new RuntimeException("调用微信 code2session 失败: " + e.getMessage(), e);
    }
    if (resp == null) {
      throw new RuntimeException("微信 code2session 返回为空");
    }
    Object errcode = resp.get("errcode");
    if (errcode instanceof Number && ((Number) errcode).intValue() != 0) {
      throw new RuntimeException("微信 code2session 错误: " + resp.get("errmsg"));
    }
    String openid = (String) resp.get("openid");
    if (openid == null || openid.isEmpty()) {
      throw new RuntimeException("微信 code2session 未返回 openid");
    }
    return openid;
  }

  /**
   * 统一的 HMAC 密钥（UTF-8 原始字节）。签发与校验必须使用同一个 SecretKey 对象，
   * 以避开 jjwt 对 String/byte[] 形式密钥做 Base64 解码、而与 signWith(String) 的
   * UTF-8 字节不一致导致校验永远失败的坑。
   */
  private javax.crypto.SecretKey signingKey() {
    return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  }

  /**
   * 为指定 openid 签发 JWT。
   */
  public String issueToken(String openid) {
    Date now = new Date();
    Date exp = new Date(now.getTime() + expireHours * 3600L * 1000);
    return Jwts.builder()
      .setSubject(openid)
      .setIssuedAt(now)
      .setExpiration(exp)
      .signWith(SignatureAlgorithm.HS256, signingKey())
      .compact();
  }

  /**
   * 校验 JWT 并返回其中的 openid；非法或过期抛出异常。
   */
  public String validateToken(String token) {
    Claims claims = Jwts.parser()
      .setSigningKey(signingKey())
      .parseClaimsJws(token)
      .getBody();
    return claims.getSubject();
  }
}
