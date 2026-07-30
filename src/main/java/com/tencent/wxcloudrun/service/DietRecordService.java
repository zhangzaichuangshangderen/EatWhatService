package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.DietRecordDayResponse;
import com.tencent.wxcloudrun.dto.DietRecordMonthResponse;
import com.tencent.wxcloudrun.dto.DietRecordUpsertRequest;

import java.time.LocalDate;
import java.time.YearMonth;

public interface DietRecordService {

  DietRecordDayResponse getDay(String userId, LocalDate date);

  DietRecordMonthResponse getMonth(String userId, YearMonth month);

  DietRecordDayResponse upsertMeal(String userId, LocalDate date, String mealKey, DietRecordUpsertRequest request);

  DietRecordDayResponse deleteMeal(String userId, LocalDate date, String mealKey);
}
