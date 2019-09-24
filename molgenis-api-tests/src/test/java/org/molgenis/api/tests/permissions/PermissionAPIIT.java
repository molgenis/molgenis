package org.molgenis.api.tests.permissions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.api.permissions.PermissionsController.OBJECTS;
import static org.molgenis.api.permissions.PermissionsController.TYPES;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.removePackages;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;
import static org.molgenis.api.tests.utils.RestTestUtils.uploadEmxFileWithoutPackage;

import com.google.common.collect.ImmutableMap;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.permissions.PermissionsController;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.api.tests.utils.RestTestUtils.Permission;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PermissionAPIIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(PermissionAPIIT.class);
  private static final String REST_TEST_USER_PASSWORD = "api_v2_test_user_password";

  private static String testUserName;
  private static String testUserToken;
  private static String testUserName2;
  private static String adminToken;

  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    testUserName = "api_v2_test_user" + System.currentTimeMillis();
    testUserName2 = "api_v2_test2_user" + System.currentTimeMillis();
    createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD);
    createUser(adminToken, testUserName2, REST_TEST_USER_PASSWORD);

    ImmutableMap.<String, Permission>builder()
        .put(RoleMetadata.ROLE, READ)
        .put(RoleMembershipMetadata.ROLE_MEMBERSHIP, READ);

    setGrantedRepositoryPermissions(
        adminToken,
        testUserName,
        ImmutableMap.<String, Permission>builder()
            .put(RoleMetadata.ROLE, READ)
            .put(RoleMembershipMetadata.ROLE_MEMBERSHIP, READ)
            .build());
    setGrantedRepositoryPermissions(
        adminToken,
        testUserName2,
        ImmutableMap.<String, Permission>builder()
            .put(RoleMetadata.ROLE, READ)
            .put(RoleMembershipMetadata.ROLE_MEMBERSHIP, READ)
            .build());
    testUserToken = RestTestUtils.login(testUserName, REST_TEST_USER_PASSWORD);
  }

  @BeforeEach
  void beforeMethod() {
    LOG.info("Importing Test data");
    uploadEmxFileWithoutPackage(adminToken, "/Permissions_TestEMX.xlsx");
    LOG.info("Importing Done");
  }

  @Test
  void testSetPermissions1() {
    /*
     * Create READ permission for test user
     * Get permission to check if it was created succesfully
     * Update permission to WRITE for test user
     * Get permission to check if it was updated succesfully
     * Delete permission for test user
     * Get permission to check if it was deleted succesfully
     **/
    String create = "{permissions:[{permission:READ,user:" + testUserName + "}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
        .then()
        .statusCode(201);

    String response1 =
        "{data={permissions=[{permission=WRITEMETA, user=admin}, {permission=READ, user="
            + testUserName
            + "}], id=perm1_entity1, label=entity1}}";

    Response actual =
        given()
            .get(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path = actual.getBody().jsonPath();
    assertEquals(response1, path.get().toString());

    String update = "{permissions:[{permission:WRITE,user:" + testUserName + "}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(update)
        .patch(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
        .then()
        .statusCode(204);

    String response2 =
        "{data={permissions=[{permission=WRITEMETA, user=admin}, {permission=WRITE, user="
            + testUserName
            + "}], id=perm1_entity1, label=entity1}}";
    Response actual2 =
        given()
            .get(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path2 = actual2.getBody().jsonPath();
    assertEquals(response2, path2.get().toString());

    String delete = "{user:" + testUserName + "}";
    given()
        .contentType(APPLICATION_JSON)
        .body(delete)
        .delete(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
        .then()
        .statusCode(204);

    String response3 =
        "{data={permissions=[{permission=WRITEMETA, user=admin}], id=perm1_entity1, label=entity1}}";
    Response actual3 =
        given()
            .get(PermissionsController.BASE_URI + "/entityType/perm1_entity1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    JsonPath path3 = actual3.getBody().jsonPath();
    assertEquals(response3, path3.getJsonObject("").toString());
  }

  @Test
  void testSetPermissionsInherited() {
    /*
     * Create READ permission for test user
     * Get permission to check if it was created succesfully
     * Update permission to WRITE for test user
     * Get permission to check if it was updated succesfully
     * Delete permission for test user
     * Get permission to check if it was deleted succesfully
     **/
    String create = "{permissions:[{permission:READ,user:" + testUserName + "}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsController.BASE_URI + "/package/perm1")
        .then()
        .statusCode(201);

    String response =
        "{data={permissions=[{permission=WRITEMETA, user=admin}, {user="
            + testUserName
            + ", inheritedPermissions=["
            + "{permission=READ, type={entityType=sys_md_Package, id=package, label=Package}, inheritedPermissions=[], object={id=perm1, label=perm1}}]}], id=perm1_entity1, label=entity1}}";

    Response actual =
        given()
            .get(PermissionsController.BASE_URI + "/entityType/perm1_entity1?inheritance=true")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    JsonPath path = actual.getBody().jsonPath();
    assertEquals(response, path.getJsonObject("").toString());

    String response2 =
        "{data={permissions=[{user="
            + testUserName
            + ", "
            + "inheritedPermissions=[{permission=READ, type={entityType=sys_md_Package, id=package, label=Package}, inheritedPermissions=[], object={id=perm1, label=perm1}}]}], "
            + "id=perm1_entity1, label=entity1}}";
    Response actual2 =
        given()
            .get(
                PermissionsController.BASE_URI
                    + "/entityType/perm1_entity1?q=user=="
                    + testUserName
                    + "&inheritance=true")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();

    JsonPath path2 = actual2.getBody().jsonPath();
    assertEquals(response2, path2.getJsonObject("").toString());
  }

  // patch for type (multiple at once) - get for type - delete as admin
  @Test
  void testSetPermissions2() {
    String create =
        "{objects:[{objectId:perm1_entity2,permissions:[{user:"
            + testUserName
            + ",permission:READMETA},"
            + "{user:"
            + testUserName2
            + ",permission:READMETA}]},"
            + "{objectId:perm2_entity3,permissions:[{user:"
            + testUserName
            + ",permission:READMETA}]}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsController.BASE_URI + "/entityType")
        .then()
        .statusCode(201);

    String request =
        "{objects:[{objectId:perm1_entity2,permissions:[{user:"
            + testUserName
            + ",permission:WRITE},{user:"
            + testUserName2
            + ",permission:READ}]},{objectId:perm2_entity3,permissions:[{user:"
            + testUserName
            + ",permission:WRITEMETA}]}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(request)
        .patch(PermissionsController.BASE_URI + "/entityType")
        .then()
        .statusCode(204);

    String response1 =
        "{objects=[{permissions=[{permission=READ, user="
            + testUserName2
            + "}, {permission=WRITE, user="
            + testUserName
            + "}], id=perm1_entity2, label=entity2}, {permissions=[{permission=WRITEMETA, user="
            + testUserName
            + "}], id=perm2_entity3, label=entity3}, {permissions=[{permission=READ, user="
            + testUserName2
            + "}, {permission=READ, user="
            + testUserName
            + "}], id=sys_sec_Role, label=Role}, {permissions=[{permission=READ, user="
            + testUserName2
            + "}, {permission=READ, user="
            + testUserName
            + "}], id=sys_sec_RoleMembership, label=Role Membership}], id=entityType, label=Entity type}";

    Response actual =
        given()
            .get(
                PermissionsController.BASE_URI
                    + "/entityType?q=user=in=("
                    + testUserName
                    + ","
                    + testUserName2
                    + ")")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path = actual.getBody().jsonPath();
    assertEquals(response1, path.getJsonObject("data").toString());
  }

  @Test
  void testCreateAcls() {
    given()
        .post(PermissionsController.BASE_URI + "/" + OBJECTS + "/entity-perm2_entity5/1")
        .then()
        .statusCode(201);

    given()
        .post(PermissionsController.BASE_URI + "/" + OBJECTS + "/entity-perm2_entity5/2")
        .then()
        .statusCode(201);

    Response actual =
        given()
            .get(PermissionsController.BASE_URI + "/" + OBJECTS + "/entity-perm2_entity5")
            .then()
            .statusCode(200)
            .extract()
            .response();
    JsonPath path = actual.getBody().jsonPath();
    assertEquals("[{id=1, label=1}, {id=2, label=2}]", path.getJsonObject("data").toString());
  }

  @Test
  void testEnableRLS() {
    given()
        .post(PermissionsController.BASE_URI + "/" + TYPES + "/entity-perm2_entity4")
        .then()
        .statusCode(201);

    Response actual =
        given()
            .get(PermissionsController.BASE_URI + "/" + TYPES + "/")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path = actual.getBody().jsonPath();
    String body = path.getJsonObject("").toString();
    assertTrue(body.contains("{entityType=sys_md_Package, id=package, label=Package}"));
    assertTrue(body.contains("{entityType=sys_md_EntityType, id=entityType, label=Entity type}"));
    assertTrue(body.contains("{entityType=sys_Plugin, id=plugin, label=Plugin}"));
    assertTrue(body.contains("{entityType=perm2_entity4, id=entity-perm2_entity4, label=entity4}"));
  }

  @Test
  void testSuitablePermissions() {
    given()
        .get(PermissionsController.BASE_URI + "/" + TYPES + "/permissions/entity-sys_FileMeta")
        .then()
        .statusCode(200)
        .body(equalTo("{\"data\":[\"READ\",\"WRITE\"]}"));
  }

  @Test
  void testGetPermissionsAsUser() {
    /*
     * Create READ permission for test user as admin
     * Get permission as user
     * Delete permission for test user as admin
     **/
    String create = "{permissions:[{permission:READ,user:" + testUserName + "}]}";
    given()
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsController.BASE_URI + "/entityType/perm1_entity2")
        .then()
        .statusCode(201);

    String response =
        "{\"data\":{\"id\":\"perm1_entity2\",\"label\":\"entity2\",\"permissions\":[{\"user\":\""
            + testUserName
            + "\",\"permission\":\"READ\"}]}}";
    given(testUserToken)
        .get(PermissionsController.BASE_URI + "/entityType/perm1_entity2?q=user==" + testUserName)
        .then()
        .statusCode(200)
        .body(equalTo(response));

    String delete = "{user:" + testUserName + "}";
    given()
        .contentType(APPLICATION_JSON)
        .body(delete)
        .delete(PermissionsController.BASE_URI + "/entityType/perm1_entity2")
        .then()
        .statusCode(204);
  }

  @AfterEach
  void afterMethod() {
    removePackages(adminToken, Arrays.asList("perm1", "perm2"));
  }

  @AfterAll
  static void afterClass() {
    cleanupUserToken(testUserToken);

    removeRightsForUser(adminToken, testUserName);
    removeRightsForUser(adminToken, testUserName2);

    AbstractApiTests.tearDownAfterClass();
  }
}
