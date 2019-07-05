package org.molgenis.api.data.v3;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractApiTest {
  private static String ADMIN_TOKEN;

  /**
   * Subclasses most call this method:
   *
   * <pre>
   * &#64;BeforeClass
   * public static void setUpBeforeClass() {
   *   AbstractApiTest.setUpBeforeClass();
   *   ...
   * }
   * </pre>
   */
  protected static void setUpBeforeClass() {
    String restTestHost = System.getProperty("API_TEST_HOST");
    if (restTestHost == null) {
      throw new IllegalArgumentException("System property 'API_TEST_HOST' undefined");
    }
    RestAssured.baseURI = restTestHost;

    String restTestAdminName = System.getProperty("API_TEST_ADMIN_NAME");
    if (restTestAdminName == null) {
      throw new IllegalArgumentException("System property 'API_TEST_ADMIN_NAME' undefined");
    }
    String restTestAdminPw = System.getProperty("API_TEST_ADMIN_PW");
    if (restTestAdminPw == null) {
      throw new IllegalArgumentException("System property 'API_TEST_ADMIN_PW' undefined");
    }

    ADMIN_TOKEN = login(restTestAdminName, restTestAdminPw);
  }

  /**
   * Subclasses most call this method:
   *
   * <pre>
   * &#64;AfterClass
   * public static void tearDownAfterClass() {
   *   AbstractApiTest.tearDownAfterClass();
   *   ...
   * }
   * </pre>
   */
  protected static void tearDownAfterClass() {
    logout();
  }

  protected static RequestSpecification given() {
    return RestAssured.given()
        .header("x-molgenis-token", ADMIN_TOKEN)
        .accept(APPLICATION_JSON_VALUE)
        .log()
        .ifValidationFails();
  }

  private static String login(String username, String password) {
    return RestAssured.given()
        .contentType(APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON_VALUE)
        .body(format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
        .log()
        .ifValidationFails()
        .post("/api/v1/login")
        .then()
        .statusCode(OK.value())
        .extract()
        .path("token");
  }

  private static void logout() {
    given().post("/api/v1/logout").then().statusCode(OK.value());
  }
}
