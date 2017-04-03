package org.molgenis.data.rest;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Resources.getResource;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.RestControllerIT.Permission.*;
import static org.molgenis.data.rest.convert.RestTestUtils.*;
import static org.testng.Assert.assertEquals;

/**
 * Tests each endpoint of the V1 Rest Api through http calls
 */
public class RestControllerV1APIIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV1APIIT.class);

	private static final String REST_TEST_USER = "api_test_user";
	private static final String REST_TEST_USER_PASSWORD = "api_test_user_password";
	private static final String V1_TEST_FILE = "/RestControllerV1_TestEMX.xlsx";
	private static final String V1_DELETE_TEST_FILE = "/RestControllerV1_DeleteEMX.xlsx";
	private static final String V1_FILE_ATTRIBUTE_TEST_FILE = "/RestControllerV1_FileEMX.xlsx";
	private static final String PATH = "api/v1/";

	private static final String TEXT_PLAIN = "text/plain";
	private static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
	private static final String TEXT_CSV = "text/csv";

	private static final String PACKAGE_PERMISSION_ID = "package_permission_ID";
	private static final String ENTITY_TYPE_PERMISSION_ID = "entityType_permission_ID";
	private static final String ATTRIBUTE_PERMISSION_ID = "attribute_permission_ID";
	private static final String FILE_META_PERMISSION_ID = "file_meta_permission_ID";
	private static final String OWNED_PERMISSION_ID = "owned_permission_ID";

	private static final String TYPE_TEST_PERMISSION_ID = "typeTest_permission_ID";
	private static final String TYPE_TEST_REF_PERMISSION_ID = "typeTestRef_permission_ID";
	private static final String LOCATION_PERMISSION_ID = "location_permission_ID";
	private static final String PERSONS_PERMISSION_ID = "persons_permission_ID";

	private static final String API_TEST_1_PERMISSION_ID = "api_test_1_permission_ID";
	private static final String API_TEST_2_PERMISSION_ID = "api_test_2_permission_ID";
	private static final String API_TEST_3_PERMISSION_ID = "api_test_3_permission_ID";
	private static final String API_TEST_4_PERMISSION_ID = "api_test_4_permission_ID";

	private static final String API_TEST_FILE_PERMISSION_ID = "api_test_file_permission_ID";

	private String testUserToken;
	private String adminToken;
	private String testUserId;

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Importing Test data");
		uploadEMX(adminToken, V1_TEST_FILE);
		uploadEMX(adminToken, V1_DELETE_TEST_FILE);
		uploadEMX(adminToken, V1_FILE_ATTRIBUTE_TEST_FILE);
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);

		grantSystemRights(adminToken, PACKAGE_PERMISSION_ID, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, ENTITY_TYPE_PERMISSION_ID, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, ATTRIBUTE_PERMISSION_ID, testUserId, "sys_md_Attribute", WRITE);
		grantSystemRights(adminToken, FILE_META_PERMISSION_ID, testUserId, "sys_FileMeta", WRITE);
		grantSystemRights(adminToken, OWNED_PERMISSION_ID, testUserId, "sys_sec_Owned", READ);

		grantRights(adminToken, TYPE_TEST_PERMISSION_ID, testUserId, "TypeTest", WRITE);
		grantRights(adminToken, TYPE_TEST_REF_PERMISSION_ID, testUserId, "TypeTestRef", WRITE);
		grantRights(adminToken, LOCATION_PERMISSION_ID, testUserId, "Location", WRITE);
		grantRights(adminToken, PERSONS_PERMISSION_ID, testUserId, "Person", WRITE);

		grantRights(adminToken, API_TEST_1_PERMISSION_ID, testUserId, "APITest1", WRITEMETA);
		grantRights(adminToken, API_TEST_2_PERMISSION_ID, testUserId, "APITest2", WRITEMETA);
		grantRights(adminToken, API_TEST_3_PERMISSION_ID, testUserId, "APITest3", WRITEMETA);
		grantRights(adminToken, API_TEST_4_PERMISSION_ID, testUserId, "APITest4", WRITEMETA);

		grantRights(adminToken, API_TEST_FILE_PERMISSION_ID, testUserId, "ApiTestFile", WRITEMETA);

		testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void testEntityExists()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(TEXT_PLAIN).when()
				.get(PATH + "it_emx_datatypes_TypeTestRef/exist").then().log().all().statusCode(200)
				.body(equalTo("true"));
	}

	@Test
	public void testEntityNotExists()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(TEXT_PLAIN).when()
				.get(PATH + "sys_NonExistingEntity/exist").then().log().all().statusCode(200).body(equalTo("false"));
	}

	@Test
	public void testGetEntityType()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef/meta").then().log()
				.all();
		validateGetEntityType(response);
	}

	@Test
	public void testGetEntityTypePost()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
				.post(PATH + "it_emx_datatypes_TypeTestRef/meta?_method=GET").then().log().all();
		validateGetEntityType(response);
	}

	@Test
	public void testRetrieveEntityAttributeMeta()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef/meta/value").then().log()
				.all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test
	public void testRetrieveEntityAttributeMetaPost()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
				.post(PATH + "it_emx_datatypes_TypeTestRef/meta/value?_method=GET").then().log().all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test
	public void testRetrieveEntity()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef/ref1").then().log()
				.all();
		validateRetrieveEntity(response);
	}

	@Test
	public void testRetrieveEntityPost()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=GET").then().log().all();
		validateRetrieveEntity(response);
	}

	@Test
	public void testRetrieveEntityAttribute()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTest/1/xxref_value").then().log()
				.all();
		validateRetrieveEntityAttribute(response);
	}

	@Test
	public void testRetrieveEntityAttributePost()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).body(new EntityCollectionRequest()).when()
				.post(PATH + "it_emx_datatypes_TypeTest/1/xxref_value?_method=GET").then().log().all();
		validateRetrieveEntityAttribute(response);
	}

	@Test
	public void testRetrieveEntityCollectionResponse()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef").then().log().all();
		validateRetrieveEntityCollectionResponse(response);
	}

	@Test
	public void testRetrieveEntityCollectionResponsePost()
	{
		ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.contentType(APPLICATION_JSON).body(new EntityCollectionRequest()).when()
				.post(PATH + "it_emx_datatypes_TypeTestRef?_method=GET").then().log().all();
		validateRetrieveEntityCollectionResponse(response);
	}

	@Test
	public void testRetrieveEntityCollection()
	{
		String contents = getFileContents("/testRetrieveEntityCollection_response.csv");
		String response = given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(TEXT_CSV).when()
				.get(PATH + "csv/it_emx_datatypes_TypeTestRef").then().contentType("text/csv").log().all()
				.statusCode(200).extract().asString();
		assertEquals(contents, response);
	}

	@Test
	public void testCreateFromFormPost()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
				.formParam("value", "ref6").formParam("label", "label6").when()
				.post(PATH + "it_emx_datatypes_TypeTestRef").then().log().all().statusCode(CREATED);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref6")
				.then().log().all().statusCode(OKE)
				.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref6"), "value", equalTo("ref6"), "label",
						equalTo("label6"));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_JSON).when()
				.delete(PATH + "it_emx_datatypes_TypeTestRef/ref6").then().log().all().statusCode(NO_CONTENT);
	}

	@Test
	public void testCreateFromFormPostMultiPart() throws URISyntaxException
	{
		URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType("multipart/form-data")
				.multiPart("id", "6").multiPart(file).when().post(PATH + "base_ApiTestFile").then().log().all()
				.statusCode(CREATED);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "base_ApiTestFile/6").then().log().all().statusCode(OKE)
				.body("href", equalTo("/api/v1/base_ApiTestFile/6"), "id", equalTo("6"), "file.href",
						equalTo("/api/v1/base_ApiTestFile/6/file"));
	}

	@Test
	public void testCreate()
	{
		Map<String, Object> entityMap = newHashMap();
		entityMap.put("value", "ref6");
		entityMap.put("label", "label6");

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_JSON).body(entityMap)
				.when().post(PATH + "it_emx_datatypes_TypeTestRef").then().log().all().statusCode(201);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "it_emx_datatypes_TypeTestRef/ref6").then().log().all().statusCode(200)
				.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref6"), "value", equalTo("ref6"), "label",
						equalTo("label6"));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.delete(PATH + "it_emx_datatypes_TypeTestRef/ref6").then().log().all().statusCode(204);
	}

	@Test
	public void testUpdate()
	{
		Map<String, Object> parameters = newHashMap();
		parameters.put("value", "ref1");
		parameters.put("label", "label900");
		given().log().all().contentType(APPLICATION_JSON).body(parameters).header(X_MOLGENIS_TOKEN, testUserToken)
				.put(PATH + "it_emx_datatypes_TypeTestRef/ref1").then().log().all().statusCode(OKE);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref1")
				.then().log().all().statusCode(OKE).body("label", equalTo("label900"));

		parameters.put("label", "label1");
		given().log().all().contentType(APPLICATION_JSON).body(parameters).header(X_MOLGENIS_TOKEN, testUserToken)
				.put(PATH + "it_emx_datatypes_TypeTestRef/ref1").then().log().all().statusCode(OKE);
	}

	@Test
	public void testUpdatePost()
	{
		Map<String, Object> parameters = newHashMap();
		parameters.put("value", "ref1");
		parameters.put("label", "label900");
		given().log().all().contentType(APPLICATION_JSON).body(parameters).header(X_MOLGENIS_TOKEN, testUserToken)
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=PUT").then().log().all().statusCode(OKE);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref1")
				.then().log().all().statusCode(OKE).body("label", equalTo("label900"));

		parameters.put("label", "label1");
		given().log().all().contentType(APPLICATION_JSON).body(parameters).header(X_MOLGENIS_TOKEN, testUserToken)
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=PUT").then().log().all().statusCode(OKE);
	}

	@Test
	public void testUpdateAttributePut()
	{
		given().log().all().contentType(APPLICATION_JSON).body("label900").header(X_MOLGENIS_TOKEN, testUserToken)
				.put(PATH + "it_emx_datatypes_TypeTestRef/ref1/label").then().log().all().statusCode(OKE);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref1")
				.then().log().all().statusCode(OKE).body("label", equalTo("label900"));

		given().log().all().contentType(APPLICATION_JSON).body("label1").header(X_MOLGENIS_TOKEN, testUserToken)
				.put(PATH + "it_emx_datatypes_TypeTestRef/ref1/label").then().log().all().statusCode(OKE);
	}

	@Test
	public void testUpdateAttribute()
	{
		given().log().all().contentType(APPLICATION_JSON).body("label900").header(X_MOLGENIS_TOKEN, testUserToken)
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1/label?_method=PUT").then().log().all().statusCode(OKE);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref1")
				.then().log().all().statusCode(OKE).body("label", equalTo("label900"));

		given().log().all().contentType(APPLICATION_JSON).body("label1").header(X_MOLGENIS_TOKEN, testUserToken)
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1/label?_method=PUT").then().log().all().statusCode(OKE);
	}

	@Test
	public void testUpdateFromFormPostMultiPart() throws URISyntaxException
	{
		URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType("multipart/form-data")
				.multiPart("id", "1").multiPart(file).when().post(PATH + "base_ApiTestFile/1?_method=PUT").then().log()
				.all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "base_ApiTestFile/1").then().log().all().statusCode(OKE)
				.body("href", equalTo("/api/v1/base_ApiTestFile/1"), "id", equalTo("1"), "file.href",
						equalTo("/api/v1/base_ApiTestFile/1/file"));
	}

	@Test
	public void testUpdateFromFormPost()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
				.formParam("value", "ref1").formParam("label", "label900").when()
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=PUT").then().log().all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref1")
				.then().log().all().statusCode(OKE).body("label", equalTo("label900"));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
				.formParam("value", "ref1").formParam("label", "label1").when()
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=PUT").then().log().all().statusCode(NO_CONTENT);
	}

	@Test
	public void testDelete()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
				.formParam("value", "ref6").formParam("label", "label6").when()
				.post(PATH + "it_emx_datatypes_TypeTestRef").then().log().all().statusCode(CREATED);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref6")
				.then().log().all().statusCode(OKE).body("value", equalTo("ref6"), "label", equalTo("label6"));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.delete(PATH + "it_emx_datatypes_TypeTestRef/ref6").then().log().all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref6")
				.then().log().all().statusCode(NOT_FOUND)
				.body("errors[0].message", equalTo("it_emx_datatypes_TypeTestRef ref6 not found"));
	}

	@Test
	public void testDeletePost()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
				.formParam("value", "ref6").formParam("label", "label6").when()
				.post(PATH + "it_emx_datatypes_TypeTestRef").then().log().all().statusCode(CREATED);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref6")
				.then().log().all().statusCode(OKE).body("value", equalTo("ref6"), "label", equalTo("label6"));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken)
				.post(PATH + "it_emx_datatypes_TypeTestRef/ref6?_method=DELETE").then().log().all()
				.statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "it_emx_datatypes_TypeTestRef/ref6")
				.then().log().all().statusCode(NOT_FOUND)
				.body("errors[0].message", equalTo("it_emx_datatypes_TypeTestRef ref6 not found"));
	}

	@Test
	public void testDeleteAll()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest1").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(40));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).delete(PATH + "base_APITest1").then().log()
				.all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest1").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(0));
	}

	@Test
	public void testDeleteAllPost()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest2").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(40));

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).post(PATH + "base_APITest2?_method=DELETE")
				.then().log().all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest2").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(0));
	}

	@Test
	public void testDeleteMeta()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest3").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(40));

		given().log().all().header(X_MOLGENIS_TOKEN, this.adminToken).delete(PATH + "base_APITest3/meta").then().log()
				.all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest3").then().log().all()
				.statusCode(NOT_FOUND).
				body("errors[0].message", equalTo("Unknown entity [base_APITest3]"));
	}

	@Test
	public void testDeleteMetaPost()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest4").then().log().all()
				.statusCode(OKE).
				body("total", equalTo(40));

		given().log().all().header(X_MOLGENIS_TOKEN, this.adminToken).post(PATH + "base_APITest4/meta?_method=DELETE")
				.then().log().all().statusCode(NO_CONTENT);

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).get(PATH + "base_APITest4").then().log().all()
				.statusCode(NOT_FOUND).
				body("errors[0].message", equalTo("Unknown entity [base_APITest4]"));
	}

	private void validateGetEntityType(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta"), "hrefCollection",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef"), "name", equalTo("it_emx_datatypes_TypeTestRef"),
				"label", equalTo("TypeTestRef"), "description", equalTo("MOLGENIS Data types test ref entity"),
				"attributes.value.href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/value"),
				"attributes.label.href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/label"), "labelAttribute",
				equalTo("label"), "idAttribute", equalTo("value"), "lookupAttributes",
				equalTo(newArrayList("value", "label")), "isAbstract", equalTo(false), "languageCode", equalTo("en"),
				"writable", equalTo(true));
	}

	private void validateRetrieveEntityAttributeMeta(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/value"), "fieldType",
				equalTo("STRING"), "name", equalTo("value"), "label", equalTo("value label"), "description",
				equalTo("TypeTestRef value attribute"), "attributes", equalTo(newArrayList()), "enumOptions",
				equalTo(newArrayList()), "maxLength", equalTo(255), "auto", equalTo(false), "nillable", equalTo(false),
				"readOnly", equalTo(true), "labelAttribute", equalTo(false), "unique", equalTo(true), "visible",
				equalTo(true), "lookupAttribute", equalTo(true), "isAggregatable", equalTo(false));
	}

	private void validateRetrieveEntity(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref1"), "value", equalTo("ref1"), "label",
				equalTo("label1"));
	}

	private void validateRetrieveEntityAttribute(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTest/1/xxref_value"), "value", equalTo("ref1"),
				"label", equalTo("label1"));
	}

	private void validateRetrieveEntityCollectionResponse(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef"), "meta.href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta"), "meta.hrefCollection",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef"), "meta.name", equalTo("it_emx_datatypes_TypeTestRef"),
				"meta.label", equalTo("TypeTestRef"), "meta.description",
				equalTo("MOLGENIS Data types test ref entity"), "meta.attributes.value.href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/value"), "meta.attributes.label.href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/label"), "meta.labelAttribute", equalTo("label"),
				"meta.idAttribute", equalTo("value"), "meta.lookupAttributes", equalTo(newArrayList("value", "label")),
				"meta.isAbstract", equalTo(false), "meta.languageCode", equalTo("en"), "meta.writable", equalTo(true),
				"start", equalTo(0), "num", equalTo(100), "total", equalTo(5), "items[0].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref1"), "items[0].value", equalTo("ref1"),
				"items[0].label", equalTo("label1"), "items[1].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref2"), "items[1].value", equalTo("ref2"),
				"items[1].label", equalTo("label2"), "items[2].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref3"), "items[2].value", equalTo("ref3"),
				"items[2].label", equalTo("label3"), "items[3].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref4"), "items[3].value", equalTo("ref4"),
				"items[3].label", equalTo("label4"), "items[4].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref5"), "items[4].value", equalTo("ref5"),
				"items[4].label", equalTo("label5"));
	}

	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntity(adminToken, "it_emx_datatypes_TypeTest");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRef");
		removeEntity(adminToken, "it_emx_datatypes_Location");
		removeEntity(adminToken, "it_emx_datatypes_Person");

		// Clean up APITest1 and 2 because they only had their rows deleted
		removeEntity(adminToken, "base_APITest1");
		removeEntity(adminToken, "base_APITest2");

		removeEntity(adminToken, "base_ApiTestFile");

		// Clean up permissions
		removeRight(adminToken, PACKAGE_PERMISSION_ID);
		removeRight(adminToken, ENTITY_TYPE_PERMISSION_ID);
		removeRight(adminToken, ATTRIBUTE_PERMISSION_ID);
		removeRight(adminToken, FILE_META_PERMISSION_ID);
		removeRight(adminToken, OWNED_PERMISSION_ID);

		removeRight(adminToken, TYPE_TEST_PERMISSION_ID);
		removeRight(adminToken, TYPE_TEST_REF_PERMISSION_ID);
		removeRight(adminToken, LOCATION_PERMISSION_ID);
		removeRight(adminToken, PERSONS_PERMISSION_ID);

		removeRight(adminToken, API_TEST_1_PERMISSION_ID);
		removeRight(adminToken, API_TEST_2_PERMISSION_ID);
		removeRight(adminToken, API_TEST_3_PERMISSION_ID);
		removeRight(adminToken, API_TEST_4_PERMISSION_ID);

		removeRight(adminToken, API_TEST_FILE_PERMISSION_ID);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, testUserToken).when().post(PATH + "logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}
}
