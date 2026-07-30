package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class IngredientCreateRequest {

  private String name;

  private String category;

  private Double kcal;

  private Double carbs;

  private Double protein;

  private Double fat;

  private Double fiber;

  private String unit;

  private String approxUnit;
}
