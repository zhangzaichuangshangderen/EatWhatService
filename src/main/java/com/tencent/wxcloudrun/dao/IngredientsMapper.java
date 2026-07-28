package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.Ingredient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IngredientsMapper {

  int createIngredient(Ingredient ingredient);

  List<Ingredient> listIngredients();

  Ingredient getIngredientById(@Param("id") Integer id);

  int updateIngredient(Ingredient ingredient);

  int softDeleteIngredient(@Param("id") Integer id);
}
