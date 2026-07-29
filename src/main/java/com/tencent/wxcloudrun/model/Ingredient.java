package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Ingredient implements Serializable {

  private Integer id;

  private String name;

  private String category;

  private Double kcal;

  private Double carbs;

  private Double protein;

  private Double fat;

  private String unit;

  private String userId;

  private Integer isDeleted;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
