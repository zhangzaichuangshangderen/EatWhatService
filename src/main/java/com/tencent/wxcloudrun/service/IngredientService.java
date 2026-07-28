package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientService {

  Ingredient createIngredient(Ingredient ingredient);

  List<Ingredient> listIngredients();

  Optional<Ingredient> getIngredient(Integer id);

  boolean updateIngredient(Ingredient ingredient);

  boolean softDeleteIngredient(Integer id);
}
