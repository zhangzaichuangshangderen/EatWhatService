package com.tencent.wxcloudrun.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Autowired
  public WxAuthService(ObjectMapper objectMapper) {
    this(objectMapper, new RestTemplate());
  }

  WxAuthService(ObjectMapper objectMapper, RestTemplate restTemplate) {
    this.objectMapper = objectMapper;
    this.restTemplate = restTemplate;
  }

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
   * 微信对 code2session 的错误响应（invalid appsecret / invalid code 等）使用
   * Content-Type: text/plain 返回（body 仍是 JSON），不能直接解析成 Map，
   * 需先取原始字符串再手动解析，才能拿到真实的 errcode/errmsg。
   */
  public String code2Session(String code) {
    if (appid.isEmpty() || secret.isEmpty()) {
      throw new IllegalStateException("微信小程序 appid/secret 尚未配置");
    }
    String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + urlEncode(appid)
      + "&secret=" + urlEncode(secret) + "&js_code=" + urlEncode(code)
      + "&grant_type=authorization_code";
    String body;
    try {
      body = restTemplate.getForObject(url, String.class);
    } catch (RestClientException e) {
      throw new RuntimeException("调用微信 code2session 失败", e);
    }
    Map<String, Object> resp = parseJson(body);
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

  private String urlEncode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException("URL 编码失败", e);
    }
  }

  private Map<String, Object> parseJson(String body) {
    if (body == null || body.isEmpty()) {
      throw new RuntimeException("微信 code2session 返回为空");
    }
    try {
      return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException("微信 code2session 返回非 JSON 内容", e);
    }
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
