package org.molgenis.api.tests.rest.v1;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.BAD_REQUEST;
import static org.molgenis.api.tests.utils.RestTestUtils.FORBIDDEN;
import static org.molgenis.api.tests.utils.RestTestUtils.NOT_FOUND;
import static org.molgenis.api.tests.utils.RestTestUtils.NO_CONTENT;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.COUNT;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITEMETA;
import static org.molgenis.api.tests.utils.RestTestUtils.UNAUTHORIZED;
import static org.molgenis.api.tests.utils.RestTestUtils.X_MOLGENIS_TOKEN;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.login;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.security.auth.UserMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RestControllerIT {
  private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

  // Request parameters
  private static final String PATH = "api/v1/";

  // User credentials
  private String testUsername;
  private static final String REST_TEST_USER_PASSWORD = "rest_test_user_password";
  private String testUserToken;
  private String adminToken;

  /**
   * Pass down system properties via the mvn commandline argument
   *
   * <p>example: mvn test -Dtest="RestControllerIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl"
   * -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
   */
  @BeforeClass
  public void beforeClass() {
    LOG.info("Read environment variables");
    String envHost = System.getProperty("REST_TEST_HOST");
    RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
    LOG.info("baseURI: " + RestAssured.baseURI);

    String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
    String adminUsername =
        Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
    LOG.info("adminUsername: " + adminUsername);

    String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
    String adminPassword =
        Strings.isNullOrEmpty(envAdminPW) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
    LOG.info("adminPassword: " + adminPassword);

    adminToken = login(adminUsername, adminPassword);

    testUsername = "rest_test_user" + System.currentTimeMillis();
    createUser(adminToken, testUsername, REST_TEST_USER_PASSWORD);

    setGrantedRepositoryPermissions(
        adminToken,
        testUsername,
        ImmutableMap.<String, Permission>builder()
            .put("sys_FreemarkerTemplate", WRITE)
            .put("sys_scr_ScriptType", READ)
            .put("sys_FileMeta", WRITEMETA)
            .put(UserMetadata.USER, COUNT)
            .build());

    testUserToken = login(testUsername, REST_TEST_USER_PASSWORD);
  }

  @Test
  public void getWithoutTokenNotAllowed() {
    // @formatter:off
    ValidatableResponse response;

    response = getWithoutToken("sys_md_EntityType");
    response
        .statusCode(UNAUTHORIZED)
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Read metadata' permission on entity type 'Entity type' with id 'sys_md_EntityType'."));

    response = getWithoutToken("sys_scr_ScriptType");
    response
        .statusCode(UNAUTHORIZED)
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Read metadata' permission on entity type 'Script type' with id 'sys_scr_ScriptType'."));
    // @formatter:on
  }

  @Test
  public void getWithTokenIsAllowed() {
    getWithToken("sys_FreemarkerTemplate", this.testUserToken).log().body().statusCode(200);
    getWithToken("sys_scr_ScriptType", this.testUserToken).log().body().statusCode(200);
  }

  @Test
  public void deleteNonExistingEntity() {
    given()
        .log()
        .uri()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .delete(PATH + "sys_FileMeta" + "/non-existing-entity_id")
        .then()
        .log()
        .all()
        .statusCode(NO_CONTENT);
  }

  @Test
  public void deleteWithoutWritePermissionFails() {
    // @formatter:off
    given()
        .log()
        .method()
        .log()
        .uri()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .delete(PATH + "sys_scr_ScriptType/R")
        .then()
        .statusCode(FORBIDDEN)
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Delete data' permission on entity type 'Script type' with id 'sys_scr_ScriptType'."));
    // @formatter:on
  }

  @Test
  public void logoutWithoutTokenFails() {
    // @formatter:off
    given()
        .log()
        .uri()
        .log()
        .method()
        .when()
        .post(PATH + "logout")
        .then()
        .statusCode(BAD_REQUEST)
        .log()
        .all();
    // @formatter:on
  }

  @Test
  public void logoutWithToken() {
    // @formatter:off
    given()
        .log()
        .uri()
        .log()
        .method()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .post(PATH + "logout")
        .then()
        .statusCode(OKE)
        .log()
        .all();

    given()
        .log()
        .uri()
        .log()
        .method()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .get(PATH + "sys_md_EntityType")
        .then()
        .statusCode(UNAUTHORIZED)
        .body("errors.code[0]", equalTo("DS04"))
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Read metadata' permission on entity type 'Entity type' with id 'sys_md_EntityType'."));

    given()
        .log()
        .uri()
        .log()
        .method()
        .when()
        .get(PATH + "sys_md_EntityType")
        .then()
        .statusCode(UNAUTHORIZED)
        .body("errors.code[0]", equalTo("DS04"))
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Read metadata' permission on entity type 'Entity type' with id 'sys_md_EntityType'."));
    // @formatter:on

    // clean up after test
    this.testUserToken = login(testUsername, REST_TEST_USER_PASSWORD);
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6575
  @Test
  public void testRetrieveResourceWithFileExtensionIdNotFound() {
    // @formatter:off
    given()
        .log()
        .uri()
        .log()
        .method()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .get(PATH + "sys_FreemarkerTemplate/test.csv")
        .then()
        .statusCode(NOT_FOUND)
        .body("errors[0].code", equalTo("D02"))
        .body(
            "errors[0].message",
            equalTo("Unknown entity with 'Id' 'test.csv' of type 'Freemarker template'."));
    // @formatter:on
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6731
  @Test
  public void testRetrieveSystemEntityTypeNotAllowed() {
    // @formatter:off
    given()
        .log()
        .all()
        .when()
        .get(PATH + "sys_App/meta")
        .then()
        .statusCode(UNAUTHORIZED)
        .body("errors.code[0]", equalTo("DS04"))
        .body(
            "errors.message[0]",
            equalTo("No 'Read metadata' permission on entity type 'App' with id 'sys_App'."));
    // @formatter:on
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6731
  @Test
  public void testRetrieveSystemEntityType() {
    // @formatter:off
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, this.testUserToken)
        .when()
        .get(PATH + "sys_sec_User/meta")
        .then()
        .statusCode(OKE)
        .body("name", equalTo("sys_sec_User"));
    // @formatter:on
  }

  private ValidatableResponse getWithoutToken(String requestedEntity) {
    // @formatter:off
    return given().log().uri().when().get(PATH + requestedEntity).then();
    // @formatter:on
  }

  private ValidatableResponse getWithToken(String requestedEntity, String token) {
    // @formatter:off
    return given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, token)
        .contentType(APPLICATION_JSON)
        .when()
        .get(PATH + requestedEntity)
        .then();
    // @formatter:on
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    // Clean up permissions
    removeRightsForUser(adminToken, testUsername);

    // Clean up Token for user
    cleanupUserToken(testUserToken);
  }
}
