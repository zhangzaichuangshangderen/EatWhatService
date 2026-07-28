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
  public Ingredient createIngredient(Ingredient ingredient) {
    ingredientsMapper.createIngredient(ingredient);
    return ingredient;
  }

  @Override
  public List<Ingredient> listIngredients() {
    return ingredientsMapper.listIngredients();
  }

  @Override
  public Optional<Ingredient> getIngredient(Integer id) {
    return Optional.ofNullable(ingredientsMapper.getIngredientById(id));
  }

  @Override
  public boolean updateIngredient(Ingredient ingredient) {
    return ingredientsMapper.updateIngredient(ingredient) > 0;
  }

  @Override
  public boolean softDeleteIngredient(Integer id) {
    return ingredientsMapper.softDeleteIngredient(id) > 0;
  }
}
