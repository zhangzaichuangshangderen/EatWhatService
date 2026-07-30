package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.dao.IngredientsMapper;
import com.tencent.wxcloudrun.model.FoodCatalogItem;
import com.tencent.wxcloudrun.model.Ingredient;
import com.tencent.wxcloudrun.service.FoodCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FoodCatalogServiceImpl implements FoodCatalogService {

  private final IngredientsMapper ingredientsMapper;
  private final List<FoodCatalogItem> presetFoods;

  public FoodCatalogServiceImpl(@Autowired IngredientsMapper ingredientsMapper, @Autowired ObjectMapper objectMapper) {
    this.ingredientsMapper = ingredientsMapper;
    this.presetFoods = loadPresetFoods(objectMapper);
  }

  @Override
  public List<FoodCatalogItem> listFoods(String userId) {
    List<Ingredient> customFoods = ingredientsMapper.listIngredients(userId);
    List<FoodCatalogItem> result = new ArrayList<>(presetFoods.size() + customFoods.size());
    result.addAll(presetFoods);
    for (Ingredient ingredient : customFoods) {
      result.add(fromIngredient(ingredient));
    }
    return result;
  }

  private List<FoodCatalogItem> loadPresetFoods(ObjectMapper objectMapper) {
    try (InputStream inputStream = new ClassPathResource("foods.json").getInputStream()) {
      List<FoodCatalogItem> foods = objectMapper.readValue(
        inputStream,
        new TypeReference<List<FoodCatalogItem>>() { }
      );
      for (FoodCatalogItem food : foods) {
        food.setCustom(false);
      }
      return Collections.unmodifiableList(foods);
    } catch (IOException exception) {
      throw new IllegalStateException("基础食材数据加载失败", exception);
    }
  }

  private FoodCatalogItem fromIngredient(Ingredient ingredient) {
    FoodCatalogItem food = new FoodCatalogItem();
    food.setId(String.valueOf(ingredient.getId()));
    food.setName(ingredient.getName());
    food.setCategory(ingredient.getCategory());
    food.setKcal(ingredient.getKcal());
    food.setCarbs(ingredient.getCarbs());
    food.setProtein(ingredient.getProtein());
    food.setFat(ingredient.getFat());
    food.setFiber(ingredient.getFiber());
    food.setUnit(ingredient.getUnit());
    food.setApproxUnit(ingredient.getApproxUnit());
    food.setCustom(true);
    food.setCreatedAt(ingredient.getCreatedAt());
    food.setUpdatedAt(ingredient.getUpdatedAt());
    return food;
  }
}
