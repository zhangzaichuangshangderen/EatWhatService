package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.IngredientCreateRequest;
import com.tencent.wxcloudrun.service.IngredientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class IngredientControllerTest {

  private IngredientService service;
  private IngredientController controller;
  private MockHttpServletRequest loggedInRequest;

  @BeforeEach
  void setUp() {
    service = mock(IngredientService.class);
    controller = new IngredientController(service);
    loggedInRequest = new MockHttpServletRequest();
    loggedInRequest.addHeader("X-WX-OPENID", "user-a");
  }

  @Test
  void rejectsMissingBody() {
    ApiResponse response = controller.create(null, loggedInRequest);

    assertEquals("食材数据不能为空", response.getErrorMsg());
    verify(service, never()).createIngredient(any(), any());
  }

  @Test
  void rejectsNonFiniteFiber() {
    IngredientCreateRequest request = validRequest();
    request.setFiber(Double.POSITIVE_INFINITY);

    ApiResponse response = controller.create(request, loggedInRequest);

    assertEquals("膳食纤维超出范围", response.getErrorMsg());
    verify(service, never()).createIngredient(any(), any());
  }

  private IngredientCreateRequest validRequest() {
    IngredientCreateRequest request = new IngredientCreateRequest();
    request.setName("测试食材");
    request.setCategory("protein");
    request.setUnit("g");
    request.setKcal(100.0);
    request.setCarbs(1.0);
    request.setProtein(20.0);
    request.setFat(1.0);
    request.setFiber(2.0);
    return request;
  }
}
