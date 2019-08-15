package org.molgenis.api.tests;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.tests.utils.RestTestUtils;

public abstract class AbstractApiTests {
  protected static final ThreadLocal<String> ADMIN_TOKENS = new ThreadLocal<>();;

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

    String adminToken = login(restTestAdminName, restTestAdminPw);
    ADMIN_TOKENS.set(adminToken);
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

  protected static String getAdminToken() {
    return ADMIN_TOKENS.get();
  }

  protected static RequestSpecification given() {
    return given(getAdminToken());
  }

  protected static RequestSpecification given(@Nullable @CheckForNull String token) {
    RequestSpecification requestSpecification = RestAssured.given();
    if (token != null) {
      requestSpecification = requestSpecification.header("x-molgenis-token", token);
    }
    return requestSpecification.accept(APPLICATION_JSON_VALUE);
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
