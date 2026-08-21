package com.tencent.wxcloudrun.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final AuthInterceptor authInterceptor;

  @Value("${upload.dir:/data/uploads}")
  private String uploadDir;

  public WebConfig(@Autowired AuthInterceptor authInterceptor) {
    this.authInterceptor = authInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor).addPathPatterns("/**");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 上传文件通过 /uploads/** 对外提供（Caddy 反代后即为 https://域名/uploads/...）
    registry.addResourceHandler("/uploads/**")
      .addResourceLocations("file:" + uploadDir + "/");
  }
}
