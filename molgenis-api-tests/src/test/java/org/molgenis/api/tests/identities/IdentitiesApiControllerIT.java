package org.molgenis.api.tests.identities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.test.IsEqualJson.isEqualJson;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.test.TestResourceUtils;
import org.springframework.http.HttpStatus;

@TestMethodOrder(OrderAnnotation.class)
class IdentitiesApiControllerIT extends AbstractApiTests {

  private static final String GROUP_NAME = "My-Group";
  private static String testUserName1;
  private static String testUserName2;
  private static String testUserName3;
  private static final String REST_TEST_USER_PASSWORD = "api_v2_test_user_password";
  private static String adminToken;
  private static String anonymousID;

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    testUserName1 = "identities_user1_" + System.currentTimeMillis();
    testUserName2 = "identities_user2_" + System.currentTimeMillis();
    testUserName3 = "identities_user3_" + System.currentTimeMillis();
    createUser(adminToken, testUserName1, REST_TEST_USER_PASSWORD);
    createUser(adminToken, testUserName2, REST_TEST_USER_PASSWORD);
    createUser(adminToken, testUserName3, REST_TEST_USER_PASSWORD);

    anonymousID =
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .get("/api/data/sys_sec_Role?q=name=q=ANONYMOUS")
            .then()
            .extract()
            .response()
            .getBody()
            .jsonPath()
            .getJsonObject("items[0].data.id")
            .toString();
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    AbstractApiTests.tearDownAfterClass();
  }

  @Test
  @Order(1)
  void testCreateGroup() throws IOException {
    String createGroupRequest = "{\"name\":\"" + GROUP_NAME + "\",\"label\":\"My-Group\"}";
    String addViewerRequest =
        "{\n"
            + "  \"roleName\": \""
            + GROUP_NAME
            + "_VIEWER\",\n"
            + "  \"username\": \""
            + testUserName1
            + "\"\n"
            + "}";
    String addEditorRequest =
        "{\n"
            + "  \"roleName\": \""
            + GROUP_NAME
            + "_EDITOR\",\n"
            + "  \"username\": \""
            + testUserName2
            + "\"\n"
            + "}";
    String addManagerRequest =
        "{\n"
            + "  \"roleName\": \""
            + GROUP_NAME
            + "_MANAGER\",\n"
            + "  \"username\": \""
            + testUserName3
            + "\"\n"
            + "}";

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(createGroupRequest)
        .post("/api/identities/group")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/identities/group/" + GROUP_NAME);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(addViewerRequest)
        .post("/api/identities/group/" + GROUP_NAME + "/member")
        .then()
        .statusCode(CREATED.value())
        .header(
            LOCATION,
            RestAssured.baseURI
                + "/api/identities/group/"
                + GROUP_NAME
                + "/member/"
                + testUserName1);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(addEditorRequest)
        .post("/api/identities/group/" + GROUP_NAME + "/member")
        .then()
        .statusCode(CREATED.value())
        .header(
            LOCATION,
            RestAssured.baseURI
                + "/api/identities/group/"
                + GROUP_NAME
                + "/member/"
                + testUserName2);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(addManagerRequest)
        .post("/api/identities/group/" + GROUP_NAME + "/member")
        .then()
        .statusCode(CREATED.value())
        .header(
            LOCATION,
            RestAssured.baseURI
                + "/api/identities/group/"
                + GROUP_NAME
                + "/member/"
                + testUserName3);

    String expectedMembersJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "members1Response.json",
            ImmutableMap.of(
                "baseUri",
                RestAssured.baseURI,
                "user1",
                testUserName1,
                "user2",
                testUserName2,
                "user3",
                testUserName3));

    String response =
        given()
            .get("/api/identities/group/" + GROUP_NAME + "/member")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedMembersJson, response, Collections.singletonList("id"));

    String expectedRoleJson =
        TestResourceUtils.getRenderedString(
            getClass(), "roleResponse.json", Collections.emptyMap());

    given()
        .get("/api/identities/group/" + GROUP_NAME + "/role")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedRoleJson, LENIENT));
  }

  @Test
  @Order(2)
  void testUpdateUserRole() throws IOException {
    String updateUserRequest =
        "{\n"
            + "  \"roleName\": \""
            + GROUP_NAME
            + "_EDITOR\",\n"
            + "  \"username\": \""
            + testUserName1
            + "\"\n"
            + "}";
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(updateUserRequest)
        .put("/api/identities/group/" + GROUP_NAME + "/member/" + testUserName1)
        .then()
        .statusCode(OK.value());

    String expectedMembersJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "members2Response.json",
            ImmutableMap.of(
                "baseUri",
                RestAssured.baseURI,
                "user1",
                testUserName1,
                "user2",
                testUserName2,
                "user3",
                testUserName3));

    String response =
        given()
            .get("/api/identities/group/" + GROUP_NAME + "/member")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedMembersJson, response, Collections.singletonList("id"));
  }

  @Test
  @Order(3)
  void testDeleteUserRole() throws IOException {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .delete("/api/identities/group/" + GROUP_NAME + "/member/" + testUserName1)
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedMembersJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "members3Response.json",
            ImmutableMap.of(
                "baseUri",
                RestAssured.baseURI,
                "user1",
                testUserName1,
                "user2",
                testUserName2,
                "user3",
                testUserName3));

    String response =
        given()
            .get("/api/identities/group/" + GROUP_NAME + "/member")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedMembersJson, response, Collections.singletonList("id"));
  }

  @Test
  @Order(4)
  void testExtendRole() throws IOException {
    String anonymousRequest = "{\"role\": \"" + GROUP_NAME + "_VIEWER\"}";
    String userRequest = "{\"role\": \"" + GROUP_NAME + "_EDITOR\"}";
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(anonymousRequest)
        .put("/api/identities/group/" + GROUP_NAME + "/role/ANONYMOUS")
        .then()
        .statusCode(NO_CONTENT.value());

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(userRequest)
        .put("/api/identities/group/" + GROUP_NAME + "/role/USER")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedV3RolesJson =
        TestResourceUtils.getRenderedString(
            getClass(), "V3RoleResponse.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    String response =
        given()
            .get("/api/data/sys_sec_Role/" + anonymousID + "/includes")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedV3RolesJson, response, Arrays.asList("id", "self"));
  }

  @Test
  @Order(5)
  void testUpdateExtendRole() throws IOException {
    String anonymousRequest = "{\"role\": \"" + GROUP_NAME + "_EDITOR\"}";
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(anonymousRequest)
        .put("/api/identities/group/" + GROUP_NAME + "/role/ANONYMOUS")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedV3RolesJson =
        TestResourceUtils.getRenderedString(
            getClass(), "V3Role2Response.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    String response =
        given()
            .get("/api/data/sys_sec_Role/" + anonymousID + "/includes")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedV3RolesJson, response, Arrays.asList("id", "self"));
  }

  @Test
  @Order(6)
  void deleteExtendRole() throws IOException {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .delete("/api/identities/group/" + GROUP_NAME + "/role/ANONYMOUS")
        .then()
        .statusCode(NO_CONTENT.value());

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .delete("/api/identities/group/" + GROUP_NAME + "/role/USER")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedV3RolesJson =
        TestResourceUtils.getRenderedString(
            getClass(), "V3Role3Response.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    String response =
        given()
            .get("/api/data/sys_sec_Role/" + anonymousID + "/includes")
            .then()
            .statusCode(OK.value())
            .extract()
            .response()
            .body()
            .asString();
    autoIdReplacingIsEqualJson(expectedV3RolesJson, response, Arrays.asList("id", "self"));
  }

  @Test
  @Order(7)
  void testDeleteGroup() {
    given().delete("/api/identities/group/" + GROUP_NAME).then().statusCode(NO_CONTENT.value());

    given()
        .get("/api/identities/group/" + GROUP_NAME + "/member")
        .then()
        .statusCode(HttpStatus.NOT_FOUND.value());
  }

  /*
   * Replace autoID's for specific fields in the response json and then match them to an 'expected' json string.
   */
  private void autoIdReplacingIsEqualJson(
      String expectedJson, String response, List<String> fields) {
    String result = response;
    for (String field : fields) {
      result =
          result.replaceAll(
              "\"" + field + "\":\\s?\"[a-zA-Z0-9/\\=\\?:\\-_]*\"", "\"" + field + "\":\"AUTOID\"");
    }
    assertThat(result, isEqualJson(expectedJson, LENIENT));
  }
}
