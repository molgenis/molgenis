package org.molgenis.api.tests.permissions;

import static java.lang.String.format;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.uploadEmxFileWithoutPackage;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.annotation.Nullable;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.molgenis.api.permissions.PermissionsController;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataPermissionsAPIIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(DataPermissionsAPIIT.class);
  private static String testUserName;
  private static String testUserToken;

  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    String adminToken = AbstractApiTests.getAdminToken();

    testUserName = "data_permissions_test_user" + System.currentTimeMillis();
    createUser(adminToken, testUserName, "password");

    testUserToken = RestTestUtils.login(testUserName, "password");

    LOG.info("Importing Test data");
    uploadEmxFileWithoutPackage(adminToken, "/Permissions_TestEMX.xlsx");
    LOG.info("Importing Done");

    createPermission("WRITE", "sys_job_MetadataUpsertJobExecution");
  }

  @ParameterizedTest
  @CsvSource({",404", "READMETA,200", "COUNT,200", "READ,200", "WRITE,200", "WRITEMETA,200"})
  void testReadMetaDataPermissions(@Nullable String permission, int expectedStatusCode) {
    String entityTypeId = "perm1_entity1";
    createPermission(permission, entityTypeId);

    given(testUserToken).get("/api/metadata/perm1_entity1").then().statusCode(expectedStatusCode);

    deletePermission(permission, entityTypeId);
  }

  @ParameterizedTest
  @CsvSource({",404", "READMETA,401", "COUNT,401", "READ,200", "WRITE,200", "WRITEMETA,200"})
  void testReadDataPermissions(@Nullable String permission, int expectedStatusCode) {
    String entityTypeId = "perm1_entity1";
    createPermission(permission, entityTypeId);

    given(testUserToken).get("/api/data/perm1_entity1").then().statusCode(expectedStatusCode);

    deletePermission(permission, entityTypeId);
  }

  // TODO testCreate & testUpdate & testDelete

  @ParameterizedTest
  @CsvSource({",404", "READMETA,401", "COUNT,401", "READ,401", "WRITE,204", "WRITEMETA,204"})
  void testWriteDataPermissions(@Nullable String permission, int expectedStatusCode) {
    String entityTypeId = "perm1_entity1";
    createPermission(permission, entityTypeId);

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("label", "test");

    given(testUserToken)
        .contentType(APPLICATION_JSON_VALUE)
        .body(jsonObject.toJSONString())
        .patch(format("/api/data/%s/1", entityTypeId))
        .then()
        .statusCode(expectedStatusCode);

    deletePermission(permission, entityTypeId);
  }

  @ParameterizedTest
  @CsvSource({",404", "READMETA,401", "COUNT,401", "READ,401", "WRITE,202", "WRITEMETA,202"})
  void testWriteMetaDataPermissions(@Nullable String permission, int expectedStatusCode) {

    String entityTypeId = "perm1_entity1";
    createPermission(permission, entityTypeId);

    JSONObject jsonObject = new JSONObject();
    JSONObject labelObject = new JSONObject();
    labelObject.put("defaultValue", "test");
    jsonObject.put("label", labelObject);

    // TODO wait for job result
    given(testUserToken)
        .contentType(APPLICATION_JSON_VALUE)
        .body(jsonObject.toJSONString())
        .patch(format("/api/metadata/%s", entityTypeId))
        .then()
        .statusCode(expectedStatusCode);

    deletePermission(permission, entityTypeId);
  }

  private static void createPermission(@Nullable String permission, String entityTypeId) {
    if (permission != null) {
      String create = format("{permissions:[{permission:%s,user:%s}]}", permission, testUserName);
      given()
          .contentType(APPLICATION_JSON)
          .body(create)
          .post(PermissionsController.BASE_URI + format("/entityType/%s", entityTypeId))
          .then()
          .statusCode(201);
    }
  }

  private static void deletePermission(@Nullable String permission, String entityTypeId) {
    if (permission != null) {
      String delete = format("{user:%s}", testUserName);
      given()
          .contentType(APPLICATION_JSON)
          .body(delete)
          .delete(PermissionsController.BASE_URI + format("/entityType/%s", entityTypeId))
          .then()
          .statusCode(204);
    }
  }
}
