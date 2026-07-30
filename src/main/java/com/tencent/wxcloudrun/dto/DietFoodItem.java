package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class DietFoodItem {

  private String id;
  private String name;
  private String category;
  private String unit;
  private Double amount;
  private Double kcal;
  private Double carbs;
  private Double protein;
  private Double fat;
  private Double fiber;
  private String approxUnit;
}
