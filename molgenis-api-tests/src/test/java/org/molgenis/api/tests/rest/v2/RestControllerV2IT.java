package org.molgenis.api.tests.rest.v2;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.NO_CONTENT;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.COUNT;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.api.tests.utils.RestTestUtils.UNAUTHORIZED;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.readJsonFile;
import static org.molgenis.api.tests.utils.RestTestUtils.removeEntities;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;
import static org.molgenis.api.tests.utils.RestTestUtils.uploadEMX;
import static org.molgenis.data.file.model.FileMetaMetadata.FILE_META;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.security.auth.UserMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(OrderAnnotation.class)
class RestControllerV2IT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(RestControllerV2IT.class);

  private static final String API_V2 = "api/v2/";

  private static final String REST_TEST_USER_PASSWORD = "Blahdiblah";

  private static String testUsername;
  private static String testUserToken;
  private static String adminToken;

  private static List<String> testEntities =
      newArrayList(
          "it_emx_datatypes_TypeTestv2",
          "it_emx_datatypes_TypeTestRefv2",
          "it_emx_datatypes_Locationv2",
          "it_emx_datatypes_Personv2");

  /**
   * Pass down system properties via the mvn commandline argument example: mvn test
   * -Dtest="RestControllerV2IT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl"
   * -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
   */
  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    LOG.info("Clean up test entities if they already exist...");
    removeEntities(adminToken, testEntities);
    LOG.info("Cleaned up existing test entities.");

    RestTestUtils.createPackage(adminToken, "base");

    LOG.info("Importing RestControllerV2_TestEMX.xlsx...");
    String fileName = "/RestControllerV2_TestEMX.xlsx";
    String status = uploadEMX(adminToken, fileName);
    if (!status.equals("FINISHED")) {
      throw new RuntimeException(
          format("Import '%s' completed with status '%s'", fileName, status));
    }
    LOG.info("Importing Done");

    testUsername = "rest_test_v2" + System.currentTimeMillis();
    createUser(adminToken, testUsername, REST_TEST_USER_PASSWORD);

    ImmutableMap.Builder<String, Permission> permissionsBuilder = ImmutableMap.builder();
    permissionsBuilder
        .put(PACKAGE, WRITE)
        .put(ENTITY_TYPE_META_DATA, WRITE)
        .put(ATTRIBUTE_META_DATA, WRITE)
        .put(FILE_META, READ)
        .put(UserMetadata.USER, COUNT);
    testEntities.forEach(entity -> permissionsBuilder.put(entity, WRITE));
    setGrantedRepositoryPermissions(adminToken, testUsername, permissionsBuilder.build());

    testUserToken = RestTestUtils.login(testUsername, REST_TEST_USER_PASSWORD);
  }

  @Test
  @Order(1)
  void testApiCorsPreflightRequest() {
    given()
        .header("Access-Control-Request-Method", "DELETE ")
        .header("Access-Control-Request-Headers", "x-molgenis-token")
        .header("Origin", "https://foo.bar.org")
        .when()
        .options(API_V2 + "version")
        .then()
        .statusCode(OKE)
        .header("Access-Control-Allow-Origin", "*")
        .header("Access-Control-Allow-Methods", "DELETE")
        .header("Access-Control-Allow-Headers", "x-molgenis-token")
        .header("Access-Control-Max-Age", "1800");
  }

  @Test
  @Order(2)
  void batchRetrieveEntityCollectionTemplateExpression() {
    ValidatableResponse response =
        given(testUserToken).get(API_V2 + "it_emx_datatypes_TypeTestv2").then();

    response.statusCode(OKE);
    response.body(
        "href",
        Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2"),
        "items[1]._href",
        Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/2"),
        "items[1].xstring_template0",
        Matchers.equalTo("lorum str2 ipsum"),
        "items[1].xstring_template1",
        Matchers.equalTo("lorum label2 ipsum ref2"),
        "items[1].xstring_template2",
        Matchers.equalTo("lorum label2,label3 ipsum ref2,ref3"));
  }

  @Test
  @Order(3)
  void batchCreate() {
    JSONObject jsonObject = new JSONObject();
    JSONArray entities = new JSONArray();

    JSONObject entity1 = new JSONObject();
    entity1.put("value", "ref55");
    entity1.put("label", "label55");
    entities.add(entity1);

    JSONObject entity2 = new JSONObject();
    entity2.put("value", "ref57");
    entity2.put("label", "label57");
    entities.add(entity2);

    jsonObject.put("entities", entities);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(jsonObject.toJSONString())
        .when()
        .post(API_V2 + "it_emx_datatypes_TypeTestRefv2")
        .then()
        .statusCode(RestTestUtils.CREATED)
        .body(
            "location",
            Matchers.equalTo(
                "/api/v2/it_emx_datatypes_TypeTestRefv2?q=value=in=(\"ref55\",\"ref57\")"));
  }

  @Test
  @Order(4)
  void batchCreateLocation() {
    JSONObject jsonObject = new JSONObject();
    JSONArray entities = new JSONArray();

    JSONObject entity = new JSONObject();
    entity.put("Chromosome", "42");
    entity.put("Position", 42);
    entities.add(entity);

    jsonObject.put("entities", entities);

    String expectedLocation = "/api/v2/it_emx_datatypes_Locationv2?q=Position=in=(\"42\")";
    String expectedHref = "/api/v2/it_emx_datatypes_Locationv2/42";

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(jsonObject.toJSONString())
        .when()
        .post(API_V2 + "it_emx_datatypes_Locationv2")
        .then()
        .statusCode(RestTestUtils.CREATED)
        .body(
            "location",
            Matchers.equalTo(expectedLocation),
            "resources[0].href",
            Matchers.equalTo(expectedHref));
  }

  @Test
  @Order(5)
  void batchCreateTypeTest() {

    JSONObject entities = readJsonFile("/createEntitiesv2.json");

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(entities.toJSONString())
        .when()
        .post(API_V2 + "it_emx_datatypes_TypeTestv2")
        .then()
        .statusCode(RestTestUtils.CREATED)
        .body(
            "location",
            Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2?q=id=in=(\"55\",\"57\")"),
            "resources[0].href",
            Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/55"),
            "resources[1].href",
            Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/57"));
  }

  @Test
  @Order(6)
  void batchUpdate() {
    JSONObject entities = readJsonFile("/updateEntitiesv2.json");

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(entities.toJSONString())
        .when()
        .put(API_V2 + "it_emx_datatypes_TypeTestv2")
        .then()
        .statusCode(OKE);
  }

  @Test
  @Order(7)
  void batchUpdateOnlyOneAttribute() {
    JSONObject jsonObject = new JSONObject();
    JSONArray entities = new JSONArray();

    JSONObject entity = new JSONObject();
    entity.put("id", 55);
    entity.put("xdatetime", "2015-01-05T08:30:00+0200");
    entities.add(entity);

    JSONObject entity2 = new JSONObject();
    entity2.put("id", 57);
    entity2.put("xdatetime", "2015-01-07T08:30:00+0200");
    entities.add(entity2);

    jsonObject.put("entities", entities);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(jsonObject.toJSONString())
        .when()
        .put(API_V2 + "it_emx_datatypes_TypeTestv2/xdatetime")
        .then()
        .statusCode(OKE);
  }

  @Test
  @Order(8)
  void batchDelete() {
    JSONObject jsonObject = new JSONObject();
    JSONArray entityIds = new JSONArray();
    entityIds.add("55");
    entityIds.add("57");
    jsonObject.put("entityIds", entityIds);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(jsonObject.toJSONString())
        .when()
        .delete(API_V2 + "it_emx_datatypes_TypeTestv2")
        .then()
        .statusCode(NO_CONTENT);
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6731
  @Test
  @Order(9)
  void testRetrieveSystemEntityCollectionAggregatesNotAllowed() {
    // @formatter:off
    given(testUserToken)
        .when()
        .get(API_V2 + "sys_App?aggs=x==isActive")
        .then()
        .statusCode(UNAUTHORIZED);
    // @formatter:on
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6731
  @Test
  void testRetrieveSystemEntityCollectionAggregates() {

    given(testUserToken)
        .when()
        .get(API_V2 + "sys_sec_User?aggs=x==active;y==superuser;distinct==active")
        .then()
        .statusCode(OKE)
        .body("aggs.matrix[0][0]", Matchers.equalTo(1));
  }

  @AfterAll
  static void afterClass() {
    // Clean up TestEMX
    removeEntities(adminToken, testEntities);

    // Clean up permissions
    removeRightsForUser(adminToken, testUsername);

    // Clean up Token for user
    cleanupUserToken(testUserToken);

    AbstractApiTests.tearDownAfterClass();
  }
}
