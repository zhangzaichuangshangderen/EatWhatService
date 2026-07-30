package com.tencent.wxcloudrun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DietRecordMonthDayResponse {

  private String date;
  private List<String> mealKeys;
}
