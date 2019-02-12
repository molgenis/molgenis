package org.molgenis.api.tests.permissions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.DEFAULT_ADMIN_NAME;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.X_MOLGENIS_TOKEN;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.login;
import static org.molgenis.api.tests.utils.RestTestUtils.removePackages;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;
import static org.molgenis.api.tests.utils.RestTestUtils.uploadEMXFileWithoutPackage;
import static org.testng.Assert.assertEquals;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.util.Arrays;
import org.molgenis.api.permissions.PermissionsApiController;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.api.tests.utils.RestTestUtils.Permission;
import org.molgenis.data.security.auth.RoleMembershipMetadata;
import org.molgenis.data.security.auth.RoleMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionAPIIT {
  private static final Logger LOG = LoggerFactory.getLogger(PermissionAPIIT.class);
  private static final String REST_TEST_USER_PASSWORD = "api_v2_test_user_password";

  private String testUserName;
  private String testUserToken;
  private String testUserName2;
  private String adminToken;

  @BeforeClass
  public void beforeClass() {
    String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
    String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
    String adminPassword =
        Strings.isNullOrEmpty(envAdminPW) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
    String adminUsername = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;

    adminToken = login(adminUsername, adminPassword);

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
    testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
  }

  @BeforeMethod
  public void beforeMethod() {
    LOG.info("Importing Test data");
    uploadEMXFileWithoutPackage(adminToken, "/Permissions_TestEMX.xlsx");
    LOG.info("Importing Done");
  }

  @Test
  public void testSetPermissions1() {
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
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
        .then()
        .statusCode(201)
        .log()
        .all();

    String response1 =
        "{permissions=[{permission=READ, user="
            + testUserName
            + "}, {permission=WRITEMETA, user=admin}]}";

    Response actual =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .get(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path = actual.getBody().jsonPath();
    assertEquals(path.get().toString(), response1);

    String update = "{permissions:[{permission:WRITE,user:" + testUserName + "}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(update)
        .patch(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
        .then()
        .statusCode(204)
        .log()
        .all();

    String response2 =
        "{permissions=[{permission=READ, user="
            + testUserName
            + "}, {permission=WRITEMETA, user=admin}]}";
    Response actual2 =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .get(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .extract()
            .response();
    JsonPath path2 = actual.getBody().jsonPath();
    assertEquals(path2.get().toString(), response2);

    String delete = "{user:" + testUserName + "}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(delete)
        .delete(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
        .then()
        .statusCode(204)
        .log()
        .all();

    String response3 = "{\"permissions\":[{\"user\":\"admin\",\"permission\":\"WRITEMETA\"}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .get(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity1")
        .then()
        .statusCode(200)
        .log()
        .all()
        .body(equalTo(response3));
  }

  // patch for type (multiple at once) - get for type - delete as admin
  @Test
  public void testSetPermissions2() {
    String create =
        "{rows:[{identifier:perm1_entity2,permissions:[{user:"
            + testUserName
            + ",permission:READMETA},{user:"
            + testUserName2
            + ",permission:READMETA}]},{identifier:perm2_entity3,permissions:[{user:"
            + testUserName
            + ",permission:READMETA}]}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsApiController.BASE_URI + "/aces/entityType")
        .then()
        .statusCode(201)
        .log()
        .all();

    String request =
        "{rows:[{identifier:perm1_entity2,permissions:[{user:"
            + testUserName
            + ",permission:WRITE},{user:"
            + testUserName2
            + ",permission:READ}]},{identifier:perm2_entity3,permissions:[{user:"
            + testUserName
            + ",permission:WRITEMETA}]}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(request)
        .patch(PermissionsApiController.BASE_URI + "/aces/entityType")
        .then()
        .statusCode(204)
        .log()
        .all();

    String response1 =
        "{identityPermissions=[{identifier=perm1_entity2, permissions=[{permission=WRITE, user="
            + testUserName
            + "}, {permission=READ, user="
            + testUserName2
            + "}], label=entity2}, {identifier=perm2_entity3, permissions=[{permission=WRITEMETA, user="
            + testUserName
            + "}], label=entity3}, {identifier=sys_sec_Role, permissions=[{permission=READ, user="
            + testUserName
            + "}, {permission=READ, user="
            + testUserName2
            + "}], label=Role}, {identifier=sys_sec_RoleMembership, permissions=[{permission=READ, user="
            + testUserName
            + "}, {permission=READ, user="
            + testUserName2
            + "}], label=Role Membership}]}";

    Response actual =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .get(
                PermissionsApiController.BASE_URI
                    + "/aces/entityType?q=user=in=("
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
    assertEquals(path.getJsonObject("data").toString(), response1);
  }

  @Test
  public void testCreateAcls() {
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .post(PermissionsApiController.BASE_URI + "/acls/entity-perm2_entity5/1")
        .then()
        .statusCode(201)
        .log()
        .all();

    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .post(PermissionsApiController.BASE_URI + "/acls/entity-perm2_entity5/2")
        .then()
        .statusCode(201)
        .log()
        .all();

    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .get(PermissionsApiController.BASE_URI + "/acls/entity-perm2_entity5")
        .then()
        .statusCode(200)
        .log()
        .all()
        .body("data", equalTo(Arrays.asList("1", "2")));
  }

  @Test
  public void testEnableRLS() {
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .post(PermissionsApiController.BASE_URI + "/classes/entity-perm2_entity4")
        .then()
        .statusCode(201)
        .log()
        .all();

    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .get(PermissionsApiController.BASE_URI + "/classes/")
        .then()
        .statusCode(200)
        .log()
        .all()
        .body(
            equalTo(
                "[\"package\",\"entity-sys_job_ResourceDownloadJobExecution\",\"entity-sys_FileMeta\",\"entity-sys_ImportRun\",\"entity-sys_job_OneClickImportJobExecution\",\"entity-sys_job_ResourceDeleteJobExecution\",\"entity-sys_job_ResourceCopyJobExecution\",\"entityType\",\"plugin\",\"entity-perm2_entity4\"]"));
  }

  @Test
  public void testSuitablePermissions() {
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .get(PermissionsApiController.BASE_URI + "/classes/permissions/entity-sys_FileMeta/")
        .then()
        .statusCode(200)
        .log()
        .all()
        .body(equalTo("[\"READ\",\"WRITE\"]"));
  }

  @Test
  public void testGetPermissionsAsUser() {
    /*
     * Create READ permission for test user as admin
     * Get permission as user
     * Delete permission for test user as admin
     **/
    String create = "{permissions:[{permission:READ,user:" + testUserName + "}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(create)
        .post(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity2")
        .then()
        .statusCode(201)
        .log()
        .all();

    String response =
        "{\"permissions\":[{\"user\":\"" + testUserName + "\",\"permission\":\"READ\"}]}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, testUserToken)
        .get(
            PermissionsApiController.BASE_URI
                + "/aces/entityType/perm1_entity2?q=user=="
                + testUserName)
        .then()
        .statusCode(200)
        .log()
        .all()
        .body(equalTo(response));

    String delete = "{user:" + testUserName + "}";
    given()
        .log()
        .all()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(delete)
        .delete(PermissionsApiController.BASE_URI + "/aces/entityType/perm1_entity2")
        .then()
        .statusCode(204)
        .log()
        .all();
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {
    removePackages(adminToken, Arrays.asList("perm1", "perm2"));
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    cleanupUserToken(testUserToken);
    cleanupUserToken(adminToken);

    removeRightsForUser(adminToken, testUserName);
    removeRightsForUser(adminToken, testUserName2);
  }
}
