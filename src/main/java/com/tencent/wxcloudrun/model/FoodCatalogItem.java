package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodCatalogItem {

  private String id;
  private String name;
  private String category;
  private Double kcal;
  private Double carbs;
  private Double protein;
  private Double fat;
  private Double fiber;
  private String unit;
  private String approxUnit;
  private Boolean custom;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
