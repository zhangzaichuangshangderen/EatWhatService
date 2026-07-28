package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class IngredientUpdateRequest {

  private String name;

  private String category;

  private Double kcal;

  private Double carbs;

  private Double protein;

  private Double fat;

  private String unit;
}
