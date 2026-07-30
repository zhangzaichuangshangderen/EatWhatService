package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.service.FoodCatalogService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class FoodCatalogController {

  private final FoodCatalogService foodCatalogService;

  public FoodCatalogController(@Autowired FoodCatalogService foodCatalogService) {
    this.foodCatalogService = foodCatalogService;
  }

  @GetMapping(value = "/api/foods")
  ApiResponse list(HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(foodCatalogService.listFoods(userId.get()));
  }
}
