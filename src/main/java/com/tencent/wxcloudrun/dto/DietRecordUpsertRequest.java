package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

@Data
public class DietRecordUpsertRequest {

  private String id;
  private Double score;
  private DietNutrition totals;
  private List<DietFoodItem> items;
  private String acceptedAt;
  private Double dayGoalKcal;
}
