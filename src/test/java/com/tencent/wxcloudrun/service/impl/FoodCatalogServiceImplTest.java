package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.dao.IngredientsMapper;
import com.tencent.wxcloudrun.model.FoodCatalogItem;
import com.tencent.wxcloudrun.model.Ingredient;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FoodCatalogServiceImplTest {

  @Test
  void combinesServerPresetFoodsWithCurrentUsersCustomFoods() {
    IngredientsMapper mapper = mock(IngredientsMapper.class);
    Ingredient custom = new Ingredient();
    custom.setId(8);
    custom.setName("测试食材");
    custom.setCategory("protein");
    custom.setKcal(100.0);
    custom.setCarbs(1.0);
    custom.setProtein(20.0);
    custom.setFat(1.0);
    custom.setFiber(2.0);
    custom.setUnit("g");
    when(mapper.listIngredients("user-a")).thenReturn(Collections.singletonList(custom));

    FoodCatalogServiceImpl service = new FoodCatalogServiceImpl(mapper, new ObjectMapper());
    List<FoodCatalogItem> foods = service.listFoods("user-a");

    assertTrue(foods.size() > 1);
    assertFalse(foods.get(0).getCustom());
    assertEquals("8", foods.get(foods.size() - 1).getId());
    assertTrue(foods.get(foods.size() - 1).getCustom());
  }
}
