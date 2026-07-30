package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DietRecord {

  private Long id;
  private String userId;
  private LocalDate recordDate;
  private String mealKey;
  private String clientRecordId;
  private Double score;
  private Double kcal;
  private Double carbs;
  private Double protein;
  private Double fat;
  private Double fiber;
  private String itemsJson;
  private String acceptedAt;
  private Double dayGoalKcal;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
