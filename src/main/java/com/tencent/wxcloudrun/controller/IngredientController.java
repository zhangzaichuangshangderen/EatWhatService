package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.IngredientCreateRequest;
import com.tencent.wxcloudrun.dto.IngredientUpdateRequest;
import com.tencent.wxcloudrun.model.Ingredient;
import com.tencent.wxcloudrun.service.IngredientService;
import com.tencent.wxcloudrun.util.WxUserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
public class IngredientController {

  private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList("combo", "carbs", "protein", "fiber", "fat"));
  private static final Set<String> VALID_UNITS = new HashSet<>(Arrays.asList("g", "ml", "个", "份"));

  final IngredientService ingredientService;
  final Logger logger;

  public IngredientController(@Autowired IngredientService ingredientService) {
    this.ingredientService = ingredientService;
    this.logger = LoggerFactory.getLogger(IngredientController.class);
  }

  @PostMapping(value = "/api/ingredients")
  ApiResponse create(@RequestBody(required = false) IngredientCreateRequest request, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }

    if (request == null) {
      return ApiResponse.error("食材数据不能为空");
    }
    String validationError = validateRequest(request.getName(), request.getCategory(), request.getUnit(), request.getKcal(),
      request.getCarbs(), request.getProtein(), request.getFat(), request.getFiber(), request.getApproxUnit());
    if (validationError != null) {
      return ApiResponse.error(validationError);
    }

    Ingredient ingredient = buildIngredient(request.getName(), request.getCategory(), request.getUnit(), request.getKcal(),
      request.getCarbs(), request.getProtein(), request.getFat(), request.getFiber(), request.getApproxUnit());
    Ingredient created = ingredientService.createIngredient(userId.get(), ingredient);
    logger.info("created ingredient id={}", created.getId());
    return ApiResponse.ok(created);
  }

  @GetMapping(value = "/api/ingredients")
  ApiResponse list(HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    return ApiResponse.ok(ingredientService.listIngredients(userId.get()));
  }

  @PutMapping(value = "/api/ingredients/{id}")
  ApiResponse update(@PathVariable Integer id, @RequestBody(required = false) IngredientUpdateRequest request,
                     HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (id == null || id <= 0) {
      return ApiResponse.error("参数id错误");
    }
    if (request == null) {
      return ApiResponse.error("食材数据不能为空");
    }
    String validationError = validateRequest(request.getName(), request.getCategory(), request.getUnit(), request.getKcal(),
      request.getCarbs(), request.getProtein(), request.getFat(), request.getFiber(), request.getApproxUnit());
    if (validationError != null) {
      return ApiResponse.error(validationError);
    }
    Optional<Ingredient> existing = ingredientService.getIngredient(userId.get(), id);
    if (!existing.isPresent()) {
      return ApiResponse.error("食材不存在");
    }

    Ingredient ingredient = buildIngredient(request.getName(), request.getCategory(), request.getUnit(), request.getKcal(),
      request.getCarbs(), request.getProtein(), request.getFat(), request.getFiber(), request.getApproxUnit());
    ingredient.setId(id);
    boolean updated = ingredientService.updateIngredient(userId.get(), ingredient);
    if (!updated) {
      return ApiResponse.error("更新失败");
    }
    Optional<Ingredient> updatedEntity = ingredientService.getIngredient(userId.get(), id);
    return ApiResponse.ok(updatedEntity.orElse(ingredient));
  }

  @DeleteMapping(value = "/api/ingredients/{id}")
  ApiResponse delete(@PathVariable Integer id, HttpServletRequest httpRequest) {
    Optional<String> userId = WxUserContext.resolveOpenId(httpRequest);
    if (!userId.isPresent()) {
      return ApiResponse.error("未登录，请从小程序访问");
    }
    if (id == null || id <= 0) {
      return ApiResponse.error("参数id错误");
    }
    boolean deleted = ingredientService.softDeleteIngredient(userId.get(), id);
    if (!deleted) {
      return ApiResponse.error("食材不存在或已删除");
    }
    logger.info("soft deleted ingredient id={}", id);
    return ApiResponse.ok();
  }

  private String validateRequest(String name, String category, String unit, Double kcal, Double carbs, Double protein,
                                 Double fat, Double fiber, String approxUnit) {
    if (name == null || name.trim().isEmpty()) {
      return "食材名称不能为空";
    }
    if (name.trim().length() > 64) {
      return "食材名称长度不能超过64";
    }
    if (category == null || !VALID_CATEGORIES.contains(category)) {
      return "食材分类错误";
    }
    if (unit == null || !VALID_UNITS.contains(unit)) {
      return "食材单位错误";
    }
    if (kcal == null || carbs == null || protein == null || fat == null) {
      return "营养成分不能为空";
    }
    if (!isInRange(kcal, 0, 9999)) {
      return "热量超出范围";
    }
    if (!isInRange(carbs, 0, 999)) {
      return "碳水超出范围";
    }
    if (!isInRange(protein, 0, 999)) {
      return "蛋白质超出范围";
    }
    if (!isInRange(fat, 0, 999)) {
      return "脂肪超出范围";
    }
    if (fiber != null && !isInRange(fiber, 0, 999)) {
      return "膳食纤维超出范围";
    }
    if (approxUnit != null && approxUnit.trim().length() > 128) {
      return "份量说明长度不能超过128";
    }
    return null;
  }

  private boolean isInRange(Double value, double min, double max) {
    return value != null && Double.isFinite(value) && value >= min && value <= max;
  }

  private Ingredient buildIngredient(String name, String category, String unit, Double kcal, Double carbs, Double protein,
                                     Double fat, Double fiber, String approxUnit) {
    Ingredient ingredient = new Ingredient();
    ingredient.setName(name.trim());
    ingredient.setCategory(category);
    ingredient.setUnit(unit);
    ingredient.setKcal(kcal);
    ingredient.setCarbs(carbs);
    ingredient.setProtein(protein);
    ingredient.setFat(fat);
    ingredient.setFiber(fiber);
    ingredient.setApproxUnit(approxUnit == null || approxUnit.trim().isEmpty() ? null : approxUnit.trim());
    return ingredient;
  }
}
