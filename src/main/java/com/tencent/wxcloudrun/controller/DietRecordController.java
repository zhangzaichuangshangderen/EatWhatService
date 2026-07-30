package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DietFoodItem;
import com.tencent.wxcloudrun.dto.DietNutrition;
import com.tencent.wxcloudrun.dto.DietRecordUpsertRequest;
import com.tencent.wxcloudrun.service.DietRecordService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class DietRecordController {

  private static final Set<String> VALID_MEALS = new HashSet<>(Arrays.asList("breakfast", "lunch", "snack", "dinner"));
  private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList("combo", "carbs", "protein", "fiber", "fat"));
  private static final Set<String> VALID_UNITS = new HashSet<>(Arrays.asList("g", "ml", "个", "份"));

  private final DietRecordService dietRecordService;

  public DietRecordController(@Autowired DietRecordService dietRecordService) {
    this.dietRecordService = dietRecordService;
  }

  @GetMapping(value = "/api/diet-records/{date}")
  ApiResponse getDay(@PathVariable String date, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    LocalDate parsedDate = parseDate(date);
    if (parsedDate == null) {
      return ApiResponse.error("日期格式错误，请使用yyyy-MM-dd");
    }
    return ApiResponse.ok(dietRecordService.getDay(userId.get(), parsedDate));
  }

  @PutMapping(value = "/api/diet-records/{date}/{mealKey}")
  ApiResponse upsertMeal(@PathVariable String date, @PathVariable String mealKey,
                         @RequestBody(required = false) DietRecordUpsertRequest request,
                         HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    LocalDate parsedDate = parseDate(date);
    if (parsedDate == null) {
      return ApiResponse.error("日期格式错误，请使用yyyy-MM-dd");
    }
    if (!VALID_MEALS.contains(mealKey)) {
      return ApiResponse.error("餐次错误");
    }
    String validationError = validateRequest(request);
    if (validationError != null) {
      return ApiResponse.error(validationError);
    }
    return ApiResponse.ok(dietRecordService.upsertMeal(userId.get(), parsedDate, mealKey, request));
  }

  @DeleteMapping(value = "/api/diet-records/{date}/{mealKey}")
  ApiResponse deleteMeal(@PathVariable String date, @PathVariable String mealKey, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    LocalDate parsedDate = parseDate(date);
    if (parsedDate == null) {
      return ApiResponse.error("日期格式错误，请使用yyyy-MM-dd");
    }
    if (!VALID_MEALS.contains(mealKey)) {
      return ApiResponse.error("餐次错误");
    }
    return ApiResponse.ok(dietRecordService.deleteMeal(userId.get(), parsedDate, mealKey));
  }

  private LocalDate parseDate(String date) {
    try {
      return LocalDate.parse(date);
    } catch (DateTimeException | NullPointerException exception) {
      return null;
    }
  }

  private String validateRequest(DietRecordUpsertRequest request) {
    if (request == null) {
      return "饮食记录不能为空";
    }
    if (request.getId() == null || request.getId().trim().isEmpty() || request.getId().trim().length() > 64) {
      return "记录id错误";
    }
    if (!isInRange(request.getScore(), 0, 100)) {
      return "得分超出范围";
    }
    String nutritionError = validateNutrition(request.getTotals(), "总营养");
    if (nutritionError != null) {
      return nutritionError;
    }
    List<DietFoodItem> items = request.getItems();
    if (items == null || items.size() > 100) {
      return "食材明细错误";
    }
    for (DietFoodItem item : items) {
      String itemError = validateItem(item);
      if (itemError != null) {
        return itemError;
      }
    }
    if (request.getAcceptedAt() == null || request.getAcceptedAt().length() > 40) {
      return "记录时间错误";
    }
    try {
      Instant.parse(request.getAcceptedAt());
    } catch (DateTimeException exception) {
      return "记录时间错误";
    }
    if (request.getDayGoalKcal() != null && !isInRange(request.getDayGoalKcal(), 1, 10000)) {
      return "每日目标热量超出范围";
    }
    return null;
  }

  private String validateNutrition(DietNutrition nutrition, String prefix) {
    if (nutrition == null) {
      return prefix + "不能为空";
    }
    if (!isInRange(nutrition.getKcal(), 0, 100000)
      || !isInRange(nutrition.getCarbs(), 0, 10000)
      || !isInRange(nutrition.getProtein(), 0, 10000)
      || !isInRange(nutrition.getFat(), 0, 10000)
      || !isInRange(nutrition.getFiber(), 0, 10000)) {
      return prefix + "超出范围";
    }
    return null;
  }

  private String validateItem(DietFoodItem item) {
    if (item == null || isBlankOrTooLong(item.getId(), 64) || isBlankOrTooLong(item.getName(), 128)) {
      return "食材明细名称或id错误";
    }
    if (!VALID_CATEGORIES.contains(item.getCategory()) || !VALID_UNITS.contains(item.getUnit())) {
      return "食材明细分类或单位错误";
    }
    if (!isInRange(item.getAmount(), 0, 2000)
      || !isInRange(item.getKcal(), 0, 10000)
      || !isInRange(item.getCarbs(), 0, 10000)
      || !isInRange(item.getProtein(), 0, 10000)
      || !isInRange(item.getFat(), 0, 10000)
      || (item.getFiber() != null && !isInRange(item.getFiber(), 0, 10000))) {
      return "食材明细营养或份量超出范围";
    }
    if (item.getApproxUnit() != null && item.getApproxUnit().trim().length() > 128) {
      return "食材份量说明长度不能超过128";
    }
    return null;
  }

  private boolean isBlankOrTooLong(String value, int maxLength) {
    return value == null || value.trim().isEmpty() || value.trim().length() > maxLength;
  }

  private boolean isInRange(Double value, double min, double max) {
    return value != null && Double.isFinite(value) && value >= min && value <= max;
  }
}
