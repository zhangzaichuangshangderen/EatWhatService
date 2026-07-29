package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientService {

  Ingredient createIngredient(String userId, Ingredient ingredient);

  List<Ingredient> listIngredients(String userId);

  Optional<Ingredient> getIngredient(String userId, Integer id);

  boolean updateIngredient(String userId, Ingredient ingredient);

  boolean softDeleteIngredient(String userId, Integer id);
}
