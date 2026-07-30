package com.tencent.wxcloudrun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DietRecordMonthResponse {

  private String month;
  private List<DietRecordMonthDayResponse> days;
}
