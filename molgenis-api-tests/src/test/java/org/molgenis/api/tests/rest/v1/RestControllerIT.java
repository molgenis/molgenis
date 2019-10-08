package org.molgenis.api.tests.rest.v1;

import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.BAD_REQUEST;
import static org.molgenis.api.tests.utils.RestTestUtils.NOT_FOUND;
import static org.molgenis.api.tests.utils.RestTestUtils.NO_CONTENT;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.COUNT;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITEMETA;
import static org.molgenis.api.tests.utils.RestTestUtils.UNAUTHORIZED;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.security.auth.UserMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RestControllerIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

  // Request parameters
  private static final String PATH = "api/v1/";

  // User credentials
  private static String testUsername;
  private static final String REST_TEST_USER_PASSWORD = "rest_test_user_password";
  private static String testUserToken;
  private static String adminToken;

  /**
   * Pass down system properties via the mvn commandline argument
   *
   * <p>example: mvn test -Dtest="RestControllerIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl"
   * -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
   */
  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

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

    testUserToken = RestTestUtils.login(testUsername, REST_TEST_USER_PASSWORD);
  }

  @Test
  void getWithoutTokenNotAllowed() {
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
  void getWithTokenIsAllowed() {
    getWithToken("sys_FreemarkerTemplate", this.testUserToken).statusCode(200);
    getWithToken("sys_scr_ScriptType", this.testUserToken).statusCode(200);
  }

  @Test
  void deleteNonExistingEntity() {
    given(testUserToken)
        .when()
        .delete(PATH + "sys_FileMeta" + "/non-existing-entity_id")
        .then()
        .statusCode(NO_CONTENT);
  }

  @Test
  void deleteWithoutWritePermissionFails() {
    // @formatter:off
    given(testUserToken)
        .when()
        .delete(PATH + "sys_scr_ScriptType/R")
        .then()
        .statusCode(UNAUTHORIZED)
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Delete data' permission on entity type 'Script type' with id 'sys_scr_ScriptType'."));
    // @formatter:on
  }

  @Test
  void logoutWithoutTokenFails() {
    // @formatter:off
    given(null).when().post(PATH + "logout").then().statusCode(BAD_REQUEST);
    // @formatter:on
  }

  @Test
  void logoutWithToken() {
    // @formatter:off
    given(testUserToken).when().post(PATH + "logout").then().statusCode(OKE);

    given(testUserToken)
        .when()
        .get(PATH + "sys_md_EntityType")
        .then()
        .statusCode(UNAUTHORIZED)
        .body("errors.code[0]", equalTo("DS04"))
        .body(
            "errors.message[0]",
            equalTo(
                "No 'Read metadata' permission on entity type 'Entity type' with id 'sys_md_EntityType'."));

    given(null)
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
    this.testUserToken = RestTestUtils.login(testUsername, REST_TEST_USER_PASSWORD);
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6575
  @Test
  void testRetrieveResourceWithFileExtensionIdNotFound() {
    // @formatter:off
    given(testUserToken)
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
  void testRetrieveSystemEntityTypeNotAllowed() {
    // @formatter:off
    given(null)
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
  void testRetrieveSystemEntityType() {
    // @formatter:off
    given(testUserToken)
        .when()
        .get(PATH + "sys_sec_User/meta")
        .then()
        .statusCode(OKE)
        .body("name", equalTo("sys_sec_User"));
    // @formatter:on
  }

  private ValidatableResponse getWithoutToken(String requestedEntity) {
    // @formatter:off
    return given(null).when().get(PATH + requestedEntity).then();
    // @formatter:on
  }

  private ValidatableResponse getWithToken(String requestedEntity, String token) {
    // @formatter:off
    return given(token).contentType(APPLICATION_JSON).when().get(PATH + requestedEntity).then();
    // @formatter:on
  }

  @AfterAll
  static void afterClass() {
    // Clean up permissions
    removeRightsForUser(adminToken, testUsername);

    // Clean up Token for user
    cleanupUserToken(testUserToken);

    AbstractApiTests.tearDownAfterClass();
  }
}
