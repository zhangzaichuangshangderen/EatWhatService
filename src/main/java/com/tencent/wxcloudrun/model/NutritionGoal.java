package com.tencent.wxcloudrun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NutritionGoal {

  @JsonIgnore
  private String userId;

  private Double targetKcal;
  private String source;
  private Double bmrKcal;
  private Double tdeeKcal;
  private String goalType;
  private LocalDateTime updatedAt;
}
