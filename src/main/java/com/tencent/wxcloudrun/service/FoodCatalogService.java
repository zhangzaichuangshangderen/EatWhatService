package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.FoodCatalogItem;

import java.util.List;

public interface FoodCatalogService {

  List<FoodCatalogItem> listFoods(String userId);
}
