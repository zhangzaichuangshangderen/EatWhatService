package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.dao.DietRecordsMapper;
import com.tencent.wxcloudrun.dto.DietFoodItem;
import com.tencent.wxcloudrun.dto.DietNutrition;
import com.tencent.wxcloudrun.dto.DietRecordDayResponse;
import com.tencent.wxcloudrun.dto.DietRecordMonthDayResponse;
import com.tencent.wxcloudrun.dto.DietRecordMonthResponse;
import com.tencent.wxcloudrun.dto.DietRecordResponse;
import com.tencent.wxcloudrun.dto.DietRecordSlotResponse;
import com.tencent.wxcloudrun.dto.DietRecordUpsertRequest;
import com.tencent.wxcloudrun.model.DietRecord;
import com.tencent.wxcloudrun.model.DietRecordMonthEntry;
import com.tencent.wxcloudrun.service.DietRecordService;
import com.tencent.wxcloudrun.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DietRecordServiceImpl implements DietRecordService {

  private static final List<String> MEAL_ORDER = Collections.unmodifiableList(
    Arrays.asList("breakfast", "lunch", "snack", "dinner")
  );
  private static final Map<String, String> MEAL_LABELS = buildMealLabels();

  private final DietRecordsMapper dietRecordsMapper;
  private final ObjectMapper objectMapper;
  private final InviteService inviteService;

  public DietRecordServiceImpl(@Autowired DietRecordsMapper dietRecordsMapper,
                               @Autowired ObjectMapper objectMapper,
                               @Autowired InviteService inviteService) {
    this.dietRecordsMapper = dietRecordsMapper;
    this.objectMapper = objectMapper;
    this.inviteService = inviteService;
  }

  @Override
  public DietRecordDayResponse getDay(String userId, LocalDate date) {
    Map<String, DietRecord> recordsByMeal = new HashMap<>();
    for (DietRecord record : dietRecordsMapper.listByDate(userId, date)) {
      recordsByMeal.put(record.getMealKey(), record);
    }

    List<DietRecordSlotResponse> slots = new ArrayList<>(MEAL_ORDER.size());
    for (String mealKey : MEAL_ORDER) {
      DietRecord record = recordsByMeal.get(mealKey);
      slots.add(new DietRecordSlotResponse(
        mealKey,
        MEAL_LABELS.get(mealKey),
        record == null ? null : toResponse(record)
      ));
    }
    return new DietRecordDayResponse(date.toString(), slots);
  }

  @Override
  public DietRecordMonthResponse getMonth(String userId, YearMonth month) {
    LocalDate startDate = month.atDay(1);
    LocalDate endDate = month.plusMonths(1).atDay(1);
    Map<LocalDate, List<String>> mealKeysByDate = new LinkedHashMap<>();
    for (DietRecordMonthEntry entry : dietRecordsMapper.listMonthEntries(userId, startDate, endDate)) {
      mealKeysByDate.computeIfAbsent(entry.getRecordDate(), ignored -> new ArrayList<>()).add(entry.getMealKey());
    }

    List<DietRecordMonthDayResponse> days = new ArrayList<>(mealKeysByDate.size());
    for (Map.Entry<LocalDate, List<String>> entry : mealKeysByDate.entrySet()) {
      days.add(new DietRecordMonthDayResponse(entry.getKey().toString(), entry.getValue()));
    }
    return new DietRecordMonthResponse(month.toString(), days);
  }

  @Override
  @Transactional
  public DietRecordDayResponse upsertMeal(String userId, LocalDate date, String mealKey,
                                           DietRecordUpsertRequest request) {
    DietRecord record = new DietRecord();
    record.setUserId(userId);
    record.setRecordDate(date);
    record.setMealKey(mealKey);
    record.setClientRecordId(request.getId());
    record.setScore(request.getScore());
    record.setKcal(request.getTotals().getKcal());
    record.setCarbs(request.getTotals().getCarbs());
    record.setProtein(request.getTotals().getProtein());
    record.setFat(request.getTotals().getFat());
    record.setFiber(request.getTotals().getFiber());
    record.setItemsJson(writeItems(request.getItems()));
    record.setAcceptedAt(request.getAcceptedAt());
    record.setDayGoalKcal(request.getDayGoalKcal());
    dietRecordsMapper.upsert(record);
    inviteService.markInviteeQualified(userId);
    return getDay(userId, date);
  }

  @Override
  @Transactional
  public DietRecordDayResponse deleteMeal(String userId, LocalDate date, String mealKey) {
    dietRecordsMapper.deleteByMeal(userId, date, mealKey);
    return getDay(userId, date);
  }

  private DietRecordResponse toResponse(DietRecord record) {
    DietNutrition totals = new DietNutrition();
    totals.setKcal(record.getKcal());
    totals.setCarbs(record.getCarbs());
    totals.setProtein(record.getProtein());
    totals.setFat(record.getFat());
    totals.setFiber(record.getFiber());

    DietRecordResponse response = new DietRecordResponse();
    response.setId(record.getClientRecordId());
    response.setDate(record.getRecordDate().toString());
    response.setMealKey(record.getMealKey());
    response.setMealLabel(MEAL_LABELS.get(record.getMealKey()));
    response.setScore(record.getScore());
    response.setTotals(totals);
    response.setItems(readItems(record.getItemsJson()));
    response.setAcceptedAt(record.getAcceptedAt());
    response.setDayGoalKcal(record.getDayGoalKcal());
    return response;
  }

  private String writeItems(List<DietFoodItem> items) {
    try {
      return objectMapper.writeValueAsString(items);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("饮食记录食材序列化失败", exception);
    }
  }

  private List<DietFoodItem> readItems(String itemsJson) {
    try {
      return objectMapper.readValue(itemsJson, new TypeReference<List<DietFoodItem>>() { });
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("饮食记录食材反序列化失败", exception);
    }
  }

  private static Map<String, String> buildMealLabels() {
    Map<String, String> labels = new HashMap<>();
    labels.put("breakfast", "早餐");
    labels.put("lunch", "午餐");
    labels.put("snack", "加餐");
    labels.put("dinner", "晚餐");
    return Collections.unmodifiableMap(labels);
  }
}
