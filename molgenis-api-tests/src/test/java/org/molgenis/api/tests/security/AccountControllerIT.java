package org.molgenis.api.tests.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.api.tests.utils.RestTestUtils.X_WWW_FORM_URLENCODED;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.waitForIndexJobs;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;

@TestMethodOrder(OrderAnnotation.class)
class AccountControllerIT extends AbstractApiTests {

  private static final String password = "password";
  private static String testUserName;
  private static final String baseUri = "/account";

  @BeforeAll
  protected static void setUpBeforeClass() {
    testUserName = "account_test_user" + System.currentTimeMillis();
    AbstractApiTests.setUpBeforeClass();

    String adminToken = AbstractApiTests.getAdminToken();

    createUser(adminToken, testUserName, password, true);

    waitForIndexJobs(adminToken);
  }

  @Test
  void changePasswordUponLogin() {
    var sessionId =
        RestAssured.given()
            .contentType(X_WWW_FORM_URLENCODED)
            .body("username=" + testUserName + "&password=" + password)
            .post("/login")
            .then()
            .statusCode(302)
            .extract()
            .sessionId();

    var body = RestAssured.given().sessionId(sessionId).get("/").then().extract().body().asString();
    assertTrue(
        body.contains("change-password-modal"), "Login should redirect to change password modal");

    var newPassword = "newpassword";
    RestAssured.given()
        .contentType(X_WWW_FORM_URLENCODED)
        .body("password1=" + newPassword + "&password2=" + newPassword)
        .sessionId(sessionId)
        .post(baseUri + "/password/change")
        .then()
        .statusCode(302);

    var bodyAfterChange =
        RestAssured.given().sessionId(sessionId).get("/").then().extract().body().asString();
    assertFalse(
        bodyAfterChange.contains("change-password-modal"),
        "Login should no longer redirect to change password modal");

    RestTestUtils.login(testUserName, newPassword);
  }
}
