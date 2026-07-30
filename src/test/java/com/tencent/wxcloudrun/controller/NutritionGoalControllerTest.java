package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.NutritionGoalUpsertRequest;
import com.tencent.wxcloudrun.service.NutritionGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NutritionGoalControllerTest {

  private NutritionGoalService service;
  private NutritionGoalController controller;
  private MockHttpServletRequest loggedInRequest;

  @BeforeEach
  void setUp() {
    service = mock(NutritionGoalService.class);
    controller = new NutritionGoalController(service);
    loggedInRequest = new MockHttpServletRequest();
    loggedInRequest.addHeader("X-WX-OPENID", "user-a");
  }

  @Test
  void rejectsUntrustedAndInvalidRequests() {
    ApiResponse unauthenticated = controller.get(new MockHttpServletRequest());
    NutritionGoalUpsertRequest invalid = validRequest();
    invalid.setTargetKcal(Double.NaN);
    ApiResponse invalidTarget = controller.upsert(invalid, loggedInRequest);

    assertEquals("未登录，请从小程序访问", unauthenticated.getErrorMsg());
    assertEquals("每日摄入目标应在800-4500 kcal之间", invalidTarget.getErrorMsg());
    verify(service, never()).upsert(any(), any());
  }

  @Test
  void calculatorGoalRequiresBmrAndTdee() {
    NutritionGoalUpsertRequest request = validRequest();
    request.setBmrKcal(null);

    ApiResponse response = controller.upsert(request, loggedInRequest);

    assertEquals("测算目标缺少基础代谢或每日消耗", response.getErrorMsg());
  }

  @Test
  void passesTrustedUserToService() {
    NutritionGoalUpsertRequest request = validRequest();

    controller.upsert(request, loggedInRequest);

    verify(service).upsert("user-a", request);
  }

  private NutritionGoalUpsertRequest validRequest() {
    NutritionGoalUpsertRequest request = new NutritionGoalUpsertRequest();
    request.setTargetKcal(1800.0);
    request.setSource("calculator");
    request.setBmrKcal(1400.0);
    request.setTdeeKcal(2100.0);
    request.setGoalType("lose");
    return request;
  }
}
