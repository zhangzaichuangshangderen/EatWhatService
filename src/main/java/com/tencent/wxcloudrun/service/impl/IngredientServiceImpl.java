package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.IngredientsMapper;
import com.tencent.wxcloudrun.model.Ingredient;
import com.tencent.wxcloudrun.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IngredientServiceImpl implements IngredientService {

  final IngredientsMapper ingredientsMapper;

  public IngredientServiceImpl(@Autowired IngredientsMapper ingredientsMapper) {
    this.ingredientsMapper = ingredientsMapper;
  }

  @Override
  public Ingredient createIngredient(String userId, Ingredient ingredient) {
    ingredient.setUserId(userId);
    ingredientsMapper.createIngredient(ingredient);
    return ingredientsMapper.getIngredientById(userId, ingredient.getId());
  }

  @Override
  public List<Ingredient> listIngredients(String userId) {
    return ingredientsMapper.listIngredients(userId);
  }

  @Override
  public Optional<Ingredient> getIngredient(String userId, Integer id) {
    return Optional.ofNullable(ingredientsMapper.getIngredientById(userId, id));
  }

  @Override
  public boolean updateIngredient(String userId, Ingredient ingredient) {
    ingredient.setUserId(userId);
    return ingredientsMapper.updateIngredient(ingredient) > 0;
  }

  @Override
  public boolean softDeleteIngredient(String userId, Integer id) {
    return ingredientsMapper.softDeleteIngredient(userId, id) > 0;
  }
}
