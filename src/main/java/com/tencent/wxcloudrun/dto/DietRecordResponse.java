package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

@Data
public class DietRecordResponse {

  private String id;
  private String date;
  private String mealKey;
  private String mealLabel;
  private Double score;
  private DietNutrition totals;
  private List<DietFoodItem> items;
  private String acceptedAt;
  private Double dayGoalKcal;
}
