package org.molgenis.api;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ApiControllerTest {
  @Test
  public void testGetApiId() {
    String apiId = "myapi";
    ApiController apiController = new ApiController(apiId, 1) {};
    assertEquals(apiController.getApiId(), apiId);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetApiIdIllegalCharacters() {
    String apiId = "my+api";
    new ApiController(apiId, 1) {};
  }

  @Test
  public void testGetApiVersion() {
    int apiVersion = 2;
    ApiController apiController = new ApiController("myapi", apiVersion) {};
    assertEquals(apiController.getApiVersion(), apiVersion);
  }

  @Test
  public void testGetApiVersionEmpty() {
    int apiVersion = 2;
    ApiController apiController = new ApiController("", apiVersion) {};
    assertEquals(apiController.getApiVersion(), apiVersion);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetApiVersionIllegalVersion() {
    int apiVersion = -1;
    ApiController apiController = new ApiController("myapi", apiVersion) {};
    assertEquals(apiController.getApiVersion(), apiVersion);
  }
}
