package org.molgenis.api.tests;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.molgenis.api.tests.utils.RestTestUtils;

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
    String restTestHost = System.getProperty("REST_TEST_HOST");
    if (restTestHost == null) {
      restTestHost = RestTestUtils.DEFAULT_HOST;
    }
    RestAssured.baseURI = restTestHost;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    String restTestAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
    if (restTestAdminName == null) {
      restTestAdminName = RestTestUtils.DEFAULT_ADMIN_NAME;
    }
    String restTestAdminPw = System.getProperty("REST_TEST_ADMIN_PW");
    if (restTestAdminPw == null) {
      restTestAdminPw = RestTestUtils.DEFAULT_ADMIN_PW;
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
        .urlEncodingEnabled(false);
  }

  private static String login(String username, String password) {
    return RestAssured.given()
        .contentType(APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON_VALUE)
        .body(format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
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
