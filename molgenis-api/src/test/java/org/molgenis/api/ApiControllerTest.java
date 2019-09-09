package org.molgenis.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ApiControllerTest {
  @Test
  void testGetApiId() {
    String apiId = "myapi";
    ApiController apiController = new ApiController(apiId, 1) {};
    assertEquals(apiController.getApiId(), apiId);
  }

  @Test
  void testGetApiIdIllegalCharacters() {
    String apiId = "my+api";
    assertThrows(IllegalArgumentException.class, () -> new ApiController(apiId, 1) {});
  }

  @Test
  void testGetApiVersion() {
    int apiVersion = 2;
    ApiController apiController = new ApiController("myapi", apiVersion) {};
    assertEquals(apiController.getApiVersion(), apiVersion);
  }

  @Test
  void testGetApiVersionEmpty() {
    int apiVersion = 2;
    ApiController apiController = new ApiController("", apiVersion) {};
    assertEquals(apiController.getApiVersion(), apiVersion);
  }

  @Test
  void testGetApiVersionIllegalVersion() {
    int apiVersion = -1;
    assertThrows(IllegalArgumentException.class, () -> new ApiController("myapi", apiVersion) {});
  }
}
