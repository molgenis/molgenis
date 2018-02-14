package org.molgenis.api.tests.rest.v1;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityTypeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
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
import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.*;

/**
 * Tests each endpoint of the V1 Rest Api through http calls
 */
public class RestControllerV1APIIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerV1APIIT.class);

	private String testUserName;
	private static final String REST_TEST_USER_PASSWORD = "api_v1_test_user_password";
	private static final String V1_TEST_FILE = "/RestControllerV1_API_TestEMX.xlsx";
	private static final String V1_DELETE_TEST_FILE = "/RestControllerV1_API_DeleteEMX.xlsx";
	private static final String V1_FILE_ATTRIBUTE_TEST_FILE = "/RestControllerV1_API_FileEMX.xlsx";
	private static final String API_V1 = "api/v1/";

	private static final String TEXT_PLAIN = "text/plain";
	private static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
	private static final String TEXT_CSV = "text/csv";

	private String testUserToken;
	private String adminToken;
	private String testUserId;

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Importing Test data");
		RestTestUtils.uploadEMX(adminToken, V1_TEST_FILE);
		RestTestUtils.uploadEMX(adminToken, V1_DELETE_TEST_FILE);
		RestTestUtils.uploadEMX(adminToken, V1_FILE_ATTRIBUTE_TEST_FILE);
		LOG.info("Importing Done");

		testUserName = "api_v1_test_user" + System.currentTimeMillis();
		RestTestUtils.createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD);

		testUserId = RestTestUtils.getUserId(adminToken, testUserName);
		LOG.info("testUserId: " + testUserId);

		setGrantedRepositoryPermissions(adminToken, testUserId,
				ImmutableMap.<String, Permission>builder().put("sys_md_Package", WRITE)
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
		testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void testEntityExists()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(TEXT_PLAIN)
			   .when()
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/exist")
			   .then()
			   .log()
			   .all()
			   .statusCode(200)
			   .body(Matchers.equalTo("true"));
	}

	@Test
	public void testEntityNotExists()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(TEXT_PLAIN)
			   .when()
			   .get(API_V1 + "sys_NonExistingEntity/exist")
			   .then()
			   .log()
			   .all()
			   .statusCode(200)
			   .body(Matchers.equalTo("false"));
	}

	@Test
	public void testGetEntityType()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .when()
											  .get(API_V1 + "V1_API_TypeTestRefAPIV1/meta")
											  .then()
											  .log()
											  .all();
		validateGetEntityType(response);
	}

	@Test
	public void testGetEntityTypePost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .body(new EntityTypeRequest())
											  .when()
											  .post(API_V1 + "V1_API_TypeTestRefAPIV1/meta?_method=GET")
											  .then()
											  .log()
											  .all();
		validateGetEntityType(response);
	}

	@Test
	public void testRetrieveEntityAttributeMeta()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .when()
											  .get(API_V1 + "V1_API_TypeTestRefAPIV1/meta/value")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test
	public void testRetrieveEntityAttributeMetaPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .body(new EntityTypeRequest())
											  .when()
											  .post(API_V1 + "V1_API_TypeTestRefAPIV1/meta/value?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test
	public void testRetrieveEntity()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .when()
											  .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntity(response);
	}

	@Test
	public void testRetrieveEntityPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .body(new EntityTypeRequest())
											  .when()
											  .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntity(response);
	}

	@Test
	public void testRetrieveEntityAttribute()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .when()
											  .get(API_V1 + "V1_API_TypeTestAPIV1/1/xxref_value")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttribute(response);
	}

	@Test
	public void testRetrieveEntityAttributePost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .body(new EntityCollectionRequest())
											  .when()
											  .post(API_V1 + "V1_API_TypeTestAPIV1/1/xxref_value?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttribute(response);
	}

	@Test
	public void testRetrieveEntityCollectionResponse()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .when()
											  .get(API_V1 + "V1_API_Items")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityCollectionResponse(response);
	}

	@Test
	public void testRetrieveEntityCollectionResponsePost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .contentType(APPLICATION_JSON)
											  .body(new EntityCollectionRequest())
											  .when()
											  .post(API_V1 + "V1_API_Items?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityCollectionResponse(response);
	}

	@Test
	public void testRetrieveEntityCollection()
	{
		String contents = getFileContents("/testRetrieveEntityCollection_response.csv");
		// workaround on windows due to git replacing \n with \r\n on checkout of file depending on git core.eol and core.autocrlf config values
		contents = contents.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");

		String response = given().log()
								 .all()
								 .header(X_MOLGENIS_TOKEN, testUserToken)
								 .contentType(TEXT_CSV)
								 .when()
								 .get(API_V1 + "csv/V1_API_Items")
								 .then()
								 .contentType("text/csv")
								 .log()
								 .all()
								 .statusCode(200)
								 .extract()
								 .asString();
		Assert.assertEquals(response, contents);
	}

	@Test
	public void testCreateFromFormPost()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_FORM_URL_ENCODED)
			   .formParam("value", "ref6")
			   .formParam("label", "label6")
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref6"), "value",
					   Matchers.equalTo("ref6"), "label", Matchers.equalTo("label6"));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .when()
			   .delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);
	}

	@Test
	public void testCreateFromFormPostMultiPart() throws URISyntaxException
	{
		URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType("multipart/form-data")
			   .multiPart("id", "6")
			   .multiPart(file)
			   .when()
			   .post(API_V1 + "base_ApiTestFile")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .when()
			   .get(API_V1 + "base_ApiTestFile/6")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("href", Matchers.equalTo("/api/v1/base_ApiTestFile/6"), "id", Matchers.equalTo("6"), "file.href",
					   Matchers.equalTo("/api/v1/base_ApiTestFile/6/file"));

		// test passes if no exception occured
	}

	@Test
	public void testCreate()
	{
		Map<String, Object> entityMap = newHashMap();
		entityMap.put("value", "ref6");
		entityMap.put("label", "label6");

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(entityMap)
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1")
			   .then()
			   .log()
			   .all()
			   .statusCode(201);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .when()
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(200)
			   .body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref6"), "value",
					   Matchers.equalTo("ref6"), "label", Matchers.equalTo("label6"));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(204);
	}

	@Test
	public void testUpdate()
	{
		Map<String, Object> parameters = newHashMap();
		parameters.put("value", "ref1");
		parameters.put("label", "label900");
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(parameters)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("label", Matchers.equalTo("label900"));

		parameters.put("label", "label1");
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(parameters)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@Test
	public void testUpdatePost()
	{
		Map<String, Object> parameters = newHashMap();
		parameters.put("value", "ref1");
		parameters.put("label", "label900");
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(parameters)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("label", Matchers.equalTo("label900"));

		parameters.put("label", "label1");
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(parameters)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@Test
	public void testUpdateAttributePut()
	{
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body("label900")
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("label", Matchers.equalTo("label900"));

		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body("label1")
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .put(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@Test
	public void testUpdateAttribute()
	{
		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body("label900")
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("label", Matchers.equalTo("label900"));

		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body("label1")
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1/label?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@Test
	public void testUpdateFromFormPostMultiPart() throws URISyntaxException
	{
		URL resourceUrl = getResource(RestControllerV1APIIT.class, V1_FILE_ATTRIBUTE_TEST_FILE);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType("multipart/form-data")
			   .multiPart("id", "1")
			   .multiPart(file)
			   .when()
			   .post(API_V1 + "base_ApiTestFile/1?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .when()
			   .get(API_V1 + "base_ApiTestFile/1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("href", Matchers.equalTo("/api/v1/base_ApiTestFile/1"), "id", Matchers.equalTo("1"), "file.href",
					   Matchers.equalTo("/api/v1/base_ApiTestFile/1/file"));
	}

	@Test
	public void testUpdateFromFormPost()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_FORM_URL_ENCODED)
			   .formParam("value", "ref1")
			   .formParam("label", "label900")
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("label", Matchers.equalTo("label900"));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_FORM_URL_ENCODED)
			   .formParam("value", "ref1")
			   .formParam("label", "label1")
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref1?_method=PUT")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);
	}

	@Test
	public void testDelete()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_FORM_URL_ENCODED)
			   .formParam("value", "ref6")
			   .formParam("label", "label6")
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("value", Matchers.equalTo("ref6"), "label", Matchers.equalTo("label6"));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .delete(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NOT_FOUND)
			   .body("errors[0].message", Matchers.equalTo("V1_API_TypeTestRefAPIV1 ref6 not found"));
	}

	@Test
	public void testDeletePost()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_FORM_URL_ENCODED)
			   .formParam("value", "ref6")
			   .formParam("label", "label6")
			   .when()
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("value", Matchers.equalTo("ref6"), "label", Matchers.equalTo("label6"));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "V1_API_TypeTestRefAPIV1/ref6?_method=DELETE")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "V1_API_TypeTestRefAPIV1/ref6")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NOT_FOUND)
			   .body("errors[0].message", Matchers.equalTo("V1_API_TypeTestRefAPIV1 ref6 not found"));
	}

	@Test
	public void testDeleteAll()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(40));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .delete(API_V1 + "base_APITest1")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest1")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(0));
	}

	@Test
	public void testDeleteAllPost()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest2")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(40));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V1 + "base_APITest2?_method=DELETE")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest2")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(0));
	}

	@Test
	public void testDeleteMeta()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest3")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(40));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .delete(API_V1 + "base_APITest3/meta")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest3")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NOT_FOUND)
			   .body("errors[0].code", Matchers.equalTo("D01"));
	}

	@Test
	public void testDeleteMetaPost()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest4")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .
					   body("total", Matchers.equalTo(40));

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, adminToken)
			   .post(API_V1 + "base_APITest4/meta?_method=DELETE")
			   .then()
			   .log()
			   .all()
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V1 + "base_APITest4")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NOT_FOUND)
			   .body("errors[0].code", Matchers.equalTo("D01"));
	}

	private void validateGetEntityType(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta"), "hrefCollection",
				Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1"), "name",
				Matchers.equalTo("V1_API_TypeTestRefAPIV1"), "label", Matchers.equalTo("TypeTestRefAPIV1"),
				"description", Matchers.equalTo("MOLGENIS Data types test ref entity"), "attributes.value.href",
				Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/value"), "attributes.label.href",
				Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/label"), "labelAttribute",
				Matchers.equalTo("label"), "idAttribute", Matchers.equalTo("value"), "lookupAttributes",
				Matchers.equalTo(newArrayList("value", "label")), "isAbstract", Matchers.equalTo(false), "languageCode",
				Matchers.equalTo("en"), "writable", Matchers.equalTo(true));
	}

	private void validateRetrieveEntityAttributeMeta(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/meta/value"), "fieldType",
				Matchers.equalTo("STRING"), "name", Matchers.equalTo("value"), "label", Matchers.equalTo("value label"),
				"description", Matchers.equalTo("TypeTestRef value attribute"), "attributes",
				Matchers.equalTo(newArrayList()), "enumOptions", Matchers.equalTo(newArrayList()), "maxLength",
				Matchers.equalTo(255), "auto", Matchers.equalTo(false), "nillable", Matchers.equalTo(false), "readOnly",
				Matchers.equalTo(true), "labelAttribute", Matchers.equalTo(false), "unique", Matchers.equalTo(true),
				"visible", Matchers.equalTo(true), "lookupAttribute", Matchers.equalTo(true), "isAggregatable",
				Matchers.equalTo(false));
	}

	private void validateRetrieveEntity(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestRefAPIV1/ref1"), "value",
				Matchers.equalTo("ref1"), "label", Matchers.equalTo("label1"));
	}

	private void validateRetrieveEntityAttribute(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", Matchers.equalTo("/api/v1/V1_API_TypeTestAPIV1/1/xxref_value"), "value",
				Matchers.equalTo("ref1"), "label", Matchers.equalTo("label1"));
	}

	private void validateRetrieveEntityCollectionResponse(ValidatableResponse response)
	{
		response.statusCode(200);
		response.body("href", Matchers.equalTo("/api/v1/V1_API_Items"), "meta.href",
				Matchers.equalTo("/api/v1/V1_API_Items/meta"), "meta.hrefCollection",
				Matchers.equalTo("/api/v1/V1_API_Items"), "meta.name", Matchers.equalTo("V1_API_Items"), "meta.label",
				Matchers.equalTo("Items"), "meta.description", Matchers.equalTo("Items"), "meta.attributes.value.href",
				Matchers.equalTo("/api/v1/V1_API_Items/meta/value"), "meta.attributes.label.href",
				Matchers.equalTo("/api/v1/V1_API_Items/meta/label"), "meta.labelAttribute", Matchers.equalTo("label"),
				"meta.idAttribute", Matchers.equalTo("value"), "meta.lookupAttributes",
				Matchers.equalTo(newArrayList("value", "label")), "meta.isAbstract", Matchers.equalTo(false),
				"meta.languageCode", Matchers.equalTo("en"), "meta.writable", Matchers.equalTo(true), "start",
				Matchers.equalTo(0), "num", Matchers.equalTo(100), "total", Matchers.equalTo(5), "items[0].href",
				Matchers.equalTo("/api/v1/V1_API_Items/ref1"), "items[0].value", Matchers.equalTo("ref1"),
				"items[0].label", Matchers.equalTo("label1"), "items[1].href",
				Matchers.equalTo("/api/v1/V1_API_Items/ref2"), "items[1].value", Matchers.equalTo("ref2"),
				"items[1].label", Matchers.equalTo("label2"), "items[2].href",
				Matchers.equalTo("/api/v1/V1_API_Items/ref3"), "items[2].value", Matchers.equalTo("ref3"),
				"items[2].label", Matchers.equalTo("label3"), "items[3].href",
				Matchers.equalTo("/api/v1/V1_API_Items/ref4"), "items[3].value", Matchers.equalTo("ref4"),
				"items[3].label", Matchers.equalTo("label4"), "items[4].href",
				Matchers.equalTo("/api/v1/V1_API_Items/ref5"), "items[4].value", Matchers.equalTo("ref5"),
				"items[4].label", Matchers.equalTo("label5"));
	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
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
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		cleanupUserToken(testUserToken);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}
}
