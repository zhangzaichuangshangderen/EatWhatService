package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件上传（自建云服务使用，替代原云托管的云存储）。
 * 当前用于小程序头像上传；文件保存在 UPLOAD_DIR 并通过 /uploads/** 静态资源对外提供。
 */
@RestController
public class UploadController {

  private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

  private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
  private static final String[] ALLOWED_EXT = { "jpg", "jpeg", "png", "gif", "webp" };

  @Value("${upload.dir:/data/uploads}")
  private String uploadDir;

  @PostMapping("/api/upload")
  ApiResponse upload(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (file == null || file.isEmpty()) {
      return ApiResponse.error("上传文件不能为空");
    }
    if (file.getSize() > MAX_SIZE) {
      return ApiResponse.error("文件大小不能超过 5MB");
    }
    String original = file.getOriginalFilename();
    String ext = "";
    if (original != null && original.contains(".")) {
      ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
    }
    boolean allowed = false;
    for (String e : ALLOWED_EXT) {
      if (e.equals(ext)) {
        allowed = true;
        break;
      }
    }
    if (!allowed) {
      return ApiResponse.error("仅支持 jpg/jpeg/png/gif/webp 图片");
    }

    String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
    try {
      Path dir = Paths.get(uploadDir);
      if (!Files.exists(dir)) {
        Files.createDirectories(dir);
      }
      Path target = dir.resolve(storedName);
      // 防止路径穿越：必须是 dir 的直接子文件
      if (!target.normalize().startsWith(dir.normalize())) {
        return ApiResponse.error("非法文件路径");
      }
      file.transferTo(target.toFile());
      logger.info("uploaded file {} by {}", storedName, userId.get());
      return ApiResponse.ok(java.util.Collections.singletonMap("url", "/uploads/" + storedName));
    } catch (IOException e) {
      logger.error("文件保存失败", e);
      return ApiResponse.error("文件保存失败");
    }
  }
}
