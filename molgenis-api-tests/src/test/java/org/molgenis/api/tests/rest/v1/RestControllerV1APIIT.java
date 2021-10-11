package org.molgenis.api.tests.rest.v1;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Resources.getResource;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.CREATED;
import static org.molgenis.api.tests.utils.RestTestUtils.NO_CONTENT;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITEMETA;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.getFileContents;
import static org.molgenis.api.tests.utils.RestTestUtils.removeEntity;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tests each endpoint of the V1 Rest Api through http calls */
class RestControllerV1APIIT extends AbstractApiTests {
  private static final Logger LOG = LoggerFactory.getLogger(RestControllerV1APIIT.class);

  private static String testUserName;
  private static final String REST_TEST_USER_PASSWORD = "api_v1_test_user_password";
  private static final String V1_TEST_FILE = "/RestControllerV1_API_TestEMX.xlsx";
  private static final String V1_DELETE_TEST_FILE = "/RestControllerV1_API_DeleteEMX.xlsx";
  private static final String V1_FILE_ATTRIBUTE_TEST_FILE = "/RestControllerV1_API_FileEMX.xlsx";
  private static final String API_V1 = "api/v1/";

  private static final String TEXT_PLAIN = "text/plain";
  private static final String APPLICATION_FORM_URL_ENCODED =
      "application/x-www-form-urlencoded; charset=UTF-8";
  private static final String TEXT_CSV = "text/csv";

  private static String testUserToken;
  private static String adminToken;

  @BeforeAll
  static void beforeClass() {
    AbstractApiTests.setUpBeforeClass();
    adminToken = AbstractApiTests.getAdminToken();

    LOG.info("Importing Test data");
    RestTestUtils.createPackage(adminToken, "base");
    RestTestUtils.uploadEmxFileToBasePackage(adminToken, V1_TEST_FILE);
    RestTestUtils.uploadEmxFileToBasePackage(adminToken, V1_DELETE_TEST_FILE);
    RestTestUtils.uploadEmxFileToBasePackage(adminToken, V1_FILE_ATTRIBUTE_TEST_FILE);
    LOG.info("Importing Done");

    testUserName = "api_v1_test_user" + System.currentTimeMillis();
    RestTestUtils.createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD, false);

    LOG.info("testUserName: " + testUserName);

    setGrantedRepositoryPermissions(
        adminToken,
        testUserName,
        ImmutableMap.<String, Permission>builder()
            .put("sys_md_Package", WRITE)
            .put("sys_md_EntityType", WRITE)
            .put("sys_md_Attribute", WRITE)
            .put("sys_FileMeta", WRITE)
            .put("V1_API_TypeTestAPIV1", WRITE)
            .put("V1_API_TypeTestRefAPIV1", WRITE)
            .put("V1_API_LocationAPIV1", WRITE)
            .put("V1_API_PersonAPIV1", WRITE)
            .put("V1_API_Items", WRITE)
            .put("base_APITest1", WRITEMETA)
            .put("base_APITest2", WRITEMETA)
            .put("base_APITest3", WRITEMETA)
            .put("base_APITest4", WRITEMETA)
            .put("base_ApiTestFile", WRITEMETA)
            .build());
    testUserToken = RestTestUtils.login(testUserName, REST_TEST_USER_PASSWORD);
  }

  @Test
  void testEntityExists() {
    given(testUserToken)
        .contentType(TEXT_PLAIN)
        .when()
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/exist")
        .then()
        .statusCode(200)
        .body(equalTo("true"));
  }

  @Test
  void testEntityNotExists() {
    given(testUserToken)
        .contentType(TEXT_PLAIN)
        .when()
        .get(API_V1 + "sys_NonExistingEntity/exist")
        .then()
        .statusCode(200)
        .body(equalTo("false"));
  }

  @Test
  void testGetEntityType() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .when()
            .get(API_V1 + "V1_API_TypeTestRefAPIV1/meta")
            .then();
    validateGetEntityType(response);
  }

  @Test
  void testGetEntityTypePost() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .body("{}")
            .when()
            .post(API_V1 + "V1_API_TypeTestRefAPIV1/meta?_method=GET")
            .then();
    validateGetEntityType(response);
  }

  @Test
  void testRetrieveEntityAttributeMeta() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .when()
            .get(API_V1 + "V1_API_TypeTestRefAPIV1/meta/value")
            .then();
    validateRetrieveEntityAttributeMeta(response);
  }

  @Test
  void testRetrieveEntityAttributeMetaPost() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .body("{}")
            .when()
            .post(API_V1 + "V1_API_TypeTestRefAPIV1/meta/value?_method=GET")
            .then();
    validateRetrieveEntityAttributeMeta(response);
  }

  @Test
  void testRetrieveEntity() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .when()
            .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
            .then();
    validateRetrieveEntity(response);
  }

  @Test
  void testRetrieveEntityPost() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .body("{}")
            .when()
            .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=GET")
            .then();
    validateRetrieveEntity(response);
  }

  @Test
  void testRetrieveEntityAttribute() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .when()
            .get(API_V1 + "V1_API_TypeTestAPIV1/1/xxref_value")
            .then();
    validateRetrieveEntityAttribute(response);
  }

  @Test
  void testRetrieveEntityAttributePost() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .body("{}")
            .when()
            .post(API_V1 + "V1_API_TypeTestAPIV1/1/xxref_value?_method=GET")
            .then();
    validateRetrieveEntityAttribute(response);
  }

  @Test
  void testRetrieveEntityCollectionResponse() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .when()
            .get(API_V1 + "V1_API_Items")
            .then();
    validateRetrieveEntityCollectionResponse(response);
  }

  @Test
  void testRetrieveEntityCollectionResponsePost() {
    ValidatableResponse response =
        given(testUserToken)
            .contentType(APPLICATION_JSON)
            .body("{}")
            .when()
            .post(API_V1 + "V1_API_Items?_method=GET")
            .then();
    validateRetrieveEntityCollectionResponse(response);
  }

  @Test
  void testRetrieveEntityCollection() {
    String contents = getFileContents("/testRetrieveEntityCollection_response.csv");
    // workaround on windows due to git replacing \n with \r\n on checkout of file depending on git
    // core.eol and core.autocrlf config values
    contents = contents.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

    String response =
        given(testUserToken)
            .accept("text/csv")
            .contentType(TEXT_CSV)
            .when()
            .get(API_V1 + "csv/V1_API_Items")
            .then()
            .contentType("text/csv")
            .statusCode(200)
            .extract()
            .asString();
    assertEquals(contents, response);
  }

  @Test
  void testCreateFromFormPost() {
    given(testUserToken)
        .contentType(APPLICATION_FORM_URL_ENCODED)
        .formParam("value", "ref6")
        .formParam("label", "label6")
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1")
        .then()
        .statusCode(CREATED);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(OKE)
        .body(
            "href",
            equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref6"),
            "value",
            equalTo("ref6"),
            "label",
            equalTo("label6"));

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .when()
        .delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(NO_CONTENT);
  }

  @Test
  void testCreateFromFormPostMultiPart() throws URISyntaxException {
    URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
    File file = new File(new URI(resourceUrl.toString()).getPath());

    given(testUserToken)
        .contentType("multipart/form-data")
        .multiPart("id", "6")
        .multiPart(file)
        .when()
        .post(API_V1 + "base_ApiTestFile")
        .then()
        .statusCode(CREATED);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .when()
        .get(API_V1 + "base_ApiTestFile/6")
        .then()
        .statusCode(OKE)
        .body(
            "href",
            equalTo("/api/v1/base_ApiTestFile/6"),
            "id",
            equalTo("6"),
            "file.href",
            equalTo("/api/v1/base_ApiTestFile/6/file"));

    // test passes if no exception occured
  }

  @Test
  void testCreate() {
    Map<String, Object> entityMap = newHashMap();
    entityMap.put("value", "ref6");
    entityMap.put("label", "label6");

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(entityMap)
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1")
        .then()
        .statusCode(201);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .when()
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(200)
        .body(
            "href",
            equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref6"),
            "value",
            equalTo("ref6"),
            "label",
            equalTo("label6"));

    given(testUserToken).delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6").then().statusCode(204);
  }

  @Test
  void testUpdate() {
    Map<String, Object> parameters = newHashMap();
    parameters.put("value", "ref1");
    parameters.put("label", "label900");
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(parameters)
        .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE)
        .body("label", equalTo("label900"));

    parameters.put("label", "label1");
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(parameters)
        .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE);
  }

  @Test
  void testUpdatePost() {
    Map<String, Object> parameters = newHashMap();
    parameters.put("value", "ref1");
    parameters.put("label", "label900");
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(parameters)
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
        .then()
        .statusCode(OKE);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE)
        .body("label", equalTo("label900"));

    parameters.put("label", "label1");
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body(parameters)
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
        .then()
        .statusCode(OKE);
  }

  @Test
  void testUpdateAttributePut() {
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body("label900")
        .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label")
        .then()
        .statusCode(OKE);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE)
        .body("label", equalTo("label900"));

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body("label1")
        .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label")
        .then()
        .statusCode(OKE);
  }

  @Test
  void testUpdateAttributePutWithEmptyValue() {
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .put(API_V1 + "V1_API_TypeTestAPIV1/1/xstringnillable")
        .then()
        .statusCode(OKE);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestAPIV1/1/xstringnillable")
        .then()
        .statusCode(OKE)
        .body("xstringnillable", nullValue());
  }

  @Test
  void testUpdateAttribute() {
    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body("label900")
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label?_method=PUT")
        .then()
        .statusCode(OKE);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE)
        .body("label", equalTo("label900"));

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .body("label1")
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label?_method=PUT")
        .then()
        .statusCode(OKE);
  }

  @Test
  void testUpdateFromFormPostMultiPart() throws URISyntaxException {
    URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
    File file = new File(new URI(resourceUrl.toString()).getPath());

    given(testUserToken)
        .contentType("multipart/form-data")
        .multiPart("id", "1")
        .multiPart(file)
        .when()
        .post(API_V1 + "base_ApiTestFile/1?_method=PUT")
        .then()
        .statusCode(NO_CONTENT);

    given(testUserToken)
        .contentType(APPLICATION_JSON)
        .when()
        .get(API_V1 + "base_ApiTestFile/1")
        .then()
        .statusCode(OKE)
        .body(
            "href",
            equalTo("/api/v1/base_ApiTestFile/1"),
            "id",
            equalTo("1"),
            "file.href",
            equalTo("/api/v1/base_ApiTestFile/1/file"));
  }

  @Test
  void testUpdateFromFormPost() {
    given(testUserToken)
        .contentType(APPLICATION_FORM_URL_ENCODED)
        .formParam("value", "ref1")
        .formParam("label", "label900")
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
        .then()
        .statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
        .then()
        .statusCode(OKE)
        .body("label", equalTo("label900"));

    given(testUserToken)
        .contentType(APPLICATION_FORM_URL_ENCODED)
        .formParam("value", "ref1")
        .formParam("label", "label1")
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
        .then()
        .statusCode(NO_CONTENT);
  }

  @Test
  void testDelete() {
    given(testUserToken)
        .contentType(APPLICATION_FORM_URL_ENCODED)
        .formParam("value", "ref6")
        .formParam("label", "label6")
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1")
        .then()
        .statusCode(CREATED);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(OKE)
        .body("value", equalTo("ref6"), "label", equalTo("label6"));

    given(testUserToken)
        .delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(RestTestUtils.NOT_FOUND)
        .body("errors[0].code", equalTo("D02"))
        .body(
            "errors[0].message",
            equalTo("Unknown entity with 'value label' 'ref6' of type 'TypeTestRefAPIV1'."));
  }

  @Test
  void testDeletePost() {
    given(testUserToken)
        .contentType(APPLICATION_FORM_URL_ENCODED)
        .formParam("value", "ref6")
        .formParam("label", "label6")
        .when()
        .post(API_V1 + "V1_API_TypeTestRefAPIV1")
        .then()
        .statusCode(CREATED);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(OKE)
        .body("value", equalTo("ref6"), "label", equalTo("label6"));

    given(testUserToken)
        .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref6?_method=DELETE")
        .then()
        .statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
        .then()
        .statusCode(RestTestUtils.NOT_FOUND)
        .body("errors[0].code", equalTo("D02"))
        .body(
            "errors[0].message",
            equalTo("Unknown entity with 'value label' 'ref6' of type 'TypeTestRefAPIV1'."));
  }

  @Test
  void testDeleteAll() {
    given(testUserToken)
        .get(API_V1 + "base_APITest1")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(40));

    given(testUserToken).delete(API_V1 + "base_APITest1").then().statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "base_APITest1")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(0));
  }

  @Test
  void testDeleteAllPost() {
    given(testUserToken)
        .get(API_V1 + "base_APITest2")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(40));

    given(testUserToken)
        .post(API_V1 + "base_APITest2?_method=DELETE")
        .then()
        .statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "base_APITest2")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(0));
  }

  @Test
  void testDeleteMeta() {
    given(testUserToken)
        .get(API_V1 + "base_APITest3")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(40));

    given().delete(API_V1 + "base_APITest3/meta").then().statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "base_APITest3")
        .then()
        .statusCode(RestTestUtils.NOT_FOUND)
        .body("errors[0].code", equalTo("D01"))
        .body("errors[0].message", equalTo("Unknown entity type 'base_APITest3'."));
  }

  @Test
  void testDeleteMetaPost() {
    given(testUserToken)
        .get(API_V1 + "base_APITest4")
        .then()
        .statusCode(OKE)
        .body("total", equalTo(40));

    given().post(API_V1 + "base_APITest4/meta?_method=DELETE").then().statusCode(NO_CONTENT);

    given(testUserToken)
        .get(API_V1 + "base_APITest4")
        .then()
        .statusCode(RestTestUtils.NOT_FOUND)
        .body("errors[0].code", equalTo("D01"))
        .body("errors[0].message", equalTo("Unknown entity type 'base_APITest4'."));
  }

  private void validateGetEntityType(ValidatableResponse response) {
    response.statusCode(200);
    response.body(
        "href",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta"),
        "hrefCollection",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1"),
        "name",
        equalTo("V1_API_TypeTestRefAPIV1"),
        "label",
        equalTo("TypeTestRefAPIV1"),
        "description",
        equalTo("MOLGENIS Data types test ref entity"),
        "attributes.value.href",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/value"),
        "attributes.label.href",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/label"),
        "labelAttribute",
        equalTo("label"),
        "idAttribute",
        equalTo("value"),
        "lookupAttributes",
        equalTo(newArrayList("value", "label")),
        "isAbstract",
        equalTo(false),
        "languageCode",
        equalTo("en"),
        "writable",
        equalTo(true));
  }

  private void validateRetrieveEntityAttributeMeta(ValidatableResponse response) {
    response.statusCode(200);
    response.body(
        "href",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/value"),
        "fieldType",
        equalTo("STRING"),
        "name",
        equalTo("value"),
        "label",
        equalTo("value label"),
        "description",
        equalTo("TypeTestRef value attribute"),
        "attributes",
        equalTo(newArrayList()),
        "enumOptions",
        equalTo(newArrayList()),
        "maxLength",
        equalTo(255),
        "auto",
        equalTo(false),
        "nillable",
        equalTo(false),
        "readOnly",
        equalTo(true),
        "labelAttribute",
        equalTo(false),
        "unique",
        equalTo(true),
        "visible",
        equalTo(true),
        "lookupAttribute",
        equalTo(true),
        "isAggregatable",
        equalTo(false));
  }

  private void validateRetrieveEntity(ValidatableResponse response) {
    response.statusCode(200);
    response.body(
        "href",
        equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref1"),
        "value",
        equalTo("ref1"),
        "label",
        equalTo("label1"));
  }

  private void validateRetrieveEntityAttribute(ValidatableResponse response) {
    response.statusCode(200);
    response.body(
        "href",
        equalTo("/api/v1/V1_API_TypeTestAPIV1/1/xxref_value"),
        "value",
        equalTo("ref1"),
        "label",
        equalTo("label1"));
  }

  private void validateRetrieveEntityCollectionResponse(ValidatableResponse response) {
    response.statusCode(200);
    response.body(
        "href",
        equalTo("/api/v1/V1_API_Items"),
        "meta.href",
        equalTo("/api/v1/V1_API_Items/meta"),
        "meta.hrefCollection",
        equalTo("/api/v1/V1_API_Items"),
        "meta.name",
        equalTo("V1_API_Items"),
        "meta.label",
        equalTo("Items"),
        "meta.description",
        equalTo("Items"),
        "meta.attributes.value.href",
        equalTo("/api/v1/V1_API_Items/meta/value"),
        "meta.attributes.label.href",
        equalTo("/api/v1/V1_API_Items/meta/label"),
        "meta.labelAttribute",
        equalTo("label"),
        "meta.idAttribute",
        equalTo("value"),
        "meta.lookupAttributes",
        equalTo(newArrayList("value", "label")),
        "meta.isAbstract",
        equalTo(false),
        "meta.languageCode",
        equalTo("en"),
        "meta.writable",
        equalTo(true),
        "start",
        equalTo(0),
        "num",
        equalTo(100),
        "total",
        equalTo(5),
        "items[0].href",
        equalTo("/api/v1/V1_API_Items/ref1"),
        "items[0].value",
        equalTo("ref1"),
        "items[0].label",
        equalTo("label1"),
        "items[1].href",
        equalTo("/api/v1/V1_API_Items/ref2"),
        "items[1].value",
        equalTo("ref2"),
        "items[1].label",
        equalTo("label2"),
        "items[2].href",
        equalTo("/api/v1/V1_API_Items/ref3"),
        "items[2].value",
        equalTo("ref3"),
        "items[2].label",
        equalTo("label3"),
        "items[3].href",
        equalTo("/api/v1/V1_API_Items/ref4"),
        "items[3].value",
        equalTo("ref4"),
        "items[3].label",
        equalTo("label4"),
        "items[4].href",
        equalTo("/api/v1/V1_API_Items/ref5"),
        "items[4].value",
        equalTo("ref5"),
        "items[4].label",
        equalTo("label5"));
  }

  @AfterAll
  static void afterClass() {
    // Clean up TestEMX
    removeEntity(adminToken, "V1_API_TypeTestAPIV1");
    removeEntity(adminToken, "V1_API_TypeTestRefAPIV1");
    removeEntity(adminToken, "V1_API_LocationAPIV1");
    removeEntity(adminToken, "V1_API_PersonAPIV1");

    // Clean up APITest1 and 2 because they only had their rows deleted
    removeEntity(adminToken, "base_APITest1");
    removeEntity(adminToken, "base_APITest2");

    removeEntity(adminToken, "base_ApiTestFile");

    removeEntity(adminToken, "V1_API_Items");

    // Clean up permissions
    removeRightsForUser(adminToken, testUserName);

    // Clean up Token for user
    cleanupUserToken(testUserToken);

    AbstractApiTests.tearDownAfterClass();
  }
}
