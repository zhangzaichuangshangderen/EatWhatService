package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.NutritionGoalUpsertRequest;
import com.tencent.wxcloudrun.service.NutritionGoalService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
public class NutritionGoalController {

  private static final Set<String> VALID_SOURCES = new HashSet<>(Arrays.asList("manual", "calculator"));
  private static final Set<String> VALID_GOAL_TYPES = new HashSet<>(Arrays.asList("maintain", "lose", "gain"));

  private final NutritionGoalService nutritionGoalService;

  public NutritionGoalController(@Autowired NutritionGoalService nutritionGoalService) {
    this.nutritionGoalService = nutritionGoalService;
  }

  @GetMapping(value = "/api/nutrition-goal")
  ApiResponse get(HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(nutritionGoalService.get(userId.get()));
  }

  @PutMapping(value = "/api/nutrition-goal")
  ApiResponse upsert(@RequestBody(required = false) NutritionGoalUpsertRequest request,
                     HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    String validationError = validateRequest(request);
    if (validationError != null) {
      return ApiResponse.error(validationError);
    }
    return ApiResponse.ok(nutritionGoalService.upsert(userId.get(), request));
  }

  private String validateRequest(NutritionGoalUpsertRequest request) {
    if (request == null) {
      return "能量目标不能为空";
    }
    if (!isInRange(request.getTargetKcal(), 800, 4500)) {
      return "每日摄入目标应在800-4500 kcal之间";
    }
    if (!VALID_SOURCES.contains(request.getSource())) {
      return "目标来源错误";
    }
    if (!VALID_GOAL_TYPES.contains(request.getGoalType())) {
      return "目标类型错误";
    }
    if (request.getBmrKcal() != null && !isInRange(request.getBmrKcal(), 500, 5000)) {
      return "基础代谢超出范围";
    }
    if (request.getTdeeKcal() != null && !isInRange(request.getTdeeKcal(), 500, 10000)) {
      return "每日消耗超出范围";
    }
    if ("calculator".equals(request.getSource())
      && (request.getBmrKcal() == null || request.getTdeeKcal() == null)) {
      return "测算目标缺少基础代谢或每日消耗";
    }
    if (request.getGender() != null && !"male".equals(request.getGender()) && !"female".equals(request.getGender())) {
      return "性别取值错误";
    }
    if (request.getAge() != null && (request.getAge() < 18 || request.getAge() > 90)) {
      return "年龄应在18-90岁之间";
    }
    if (request.getHeight() != null && !isInRange(request.getHeight(), 120, 220)) {
      return "身高应在120-220 cm之间";
    }
    if (request.getWeight() != null && !isInRange(request.getWeight(), 25, 250)) {
      return "体重应在25-250 kg之间";
    }
    if (request.getOccupationIndex() != null && (request.getOccupationIndex() < 0 || request.getOccupationIndex() > 2)) {
      return "职业活动等级取值错误";
    }
    if (request.getExerciseLevelIndex() != null && (request.getExerciseLevelIndex() < 0 || request.getExerciseLevelIndex() > 3)) {
      return "运动等级取值错误";
    }
    return null;
  }

  private boolean isInRange(Double value, double min, double max) {
    return value != null && Double.isFinite(value) && value >= min && value <= max;
  }
}
