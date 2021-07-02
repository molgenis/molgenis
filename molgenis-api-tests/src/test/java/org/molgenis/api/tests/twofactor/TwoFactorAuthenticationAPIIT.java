package org.molgenis.api.tests.twofactor;

import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.UNAUTHORIZED;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;

import com.google.gson.Gson;
import io.restassured.response.ValidatableResponse;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;

class TwoFactorAuthenticationAPIIT extends AbstractApiTests {
  // Request parameters
  private static final String PATH = "api/v1/";

  // User credentials
  private static String testUsername;
  private static final String TWO_FA_AUTH_TEST_USER_PASSWORD = "two_fa_auth_test_user_password";

  private static String adminToken;
  private static String testUserToken;

  /**
   * Pass down system properties via the mvn commandline argument
   *
   * <p>example: mvn test -Dtest="TwoFactorAuthenticaitonAPIIT"
   * -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin"
   * -DREST_TEST_ADMIN_PW="admin"
   */
  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    testUsername = "two_fa_auth_test_user" + System.currentTimeMillis();
    createUser(adminToken, testUsername, TWO_FA_AUTH_TEST_USER_PASSWORD);
  }

  @Test
  void test2faEnforced() {
    toggle2fa(TwoFactorAuthenticationSetting.ENFORCED);

    try {
      Gson gson = new Gson();
      Map<String, String> loginBody = new HashMap<>();
      loginBody.put("username", testUsername);
      loginBody.put("password", TWO_FA_AUTH_TEST_USER_PASSWORD);

      given()
          .contentType(APPLICATION_JSON)
          .body(gson.toJson(loginBody))
          .when()
          .post(PATH + "login")
          .then()
          .statusCode(UNAUTHORIZED)
          .body(
              "errors.message[0]",
              Matchers.equalTo(
                  "Login using /api/v1/login is disabled, two factor authentication is enabled"));
    } finally {
      // disable 2fa in finally clause instead of after each method due to
      // https://github.com/cbeust/testng/issues/952 which results in cross-test-class issues
      toggle2fa(TwoFactorAuthenticationSetting.DISABLED);
    }
  }

  @Test
  void test2faEnabled() {
    toggle2fa(TwoFactorAuthenticationSetting.ENABLED);

    try {
      Gson gson = new Gson();
      Map<String, String> loginBody = new HashMap<>();
      loginBody.put("username", testUsername);
      loginBody.put("password", TWO_FA_AUTH_TEST_USER_PASSWORD);

      ValidatableResponse response =
          given()
              .contentType(APPLICATION_JSON)
              .body(gson.toJson(loginBody))
              .when()
              .post(PATH + "login")
              .then()
              .statusCode(OKE);

      testUserToken = response.extract().path("token");
    } finally {
      // disable 2fa in finally clause instead of after each method due to
      // https://github.com/cbeust/testng/issues/952 which results in cross-test-class issues
      toggle2fa(TwoFactorAuthenticationSetting.DISABLED);
    }
  }

  @AfterAll
  static void afterClass() {
    // Clean up permissions
    removeRightsForUser(adminToken, testUsername);

    // Clean up Token for user
    cleanupUserToken(testUserToken);

    AbstractApiTests.tearDownAfterClass();
  }

  /**
   * Enable or disable 2 factor authentication
   *
   * @param state state of 2 factor authentication (can be Enforced, Enabled, Disabled)
   */
  private void toggle2fa(TwoFactorAuthenticationSetting state) {
    given()
        .contentType(APPLICATION_JSON)
        .body(state.getLabel())
        .when()
        .put("api/v1/sys_set_auth/auth/sign_in_2fa")
        .then()
        .statusCode(200);
  }
}
