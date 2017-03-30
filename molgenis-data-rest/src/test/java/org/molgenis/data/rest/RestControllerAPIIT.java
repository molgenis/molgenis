package org.molgenis.data.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.restassured.RestAssured.*;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.convert.RestTestUtils.*;

/**
 * Tests each endpoint of the V1 Rest Api though http calls
 */
public class RestControllerAPIIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerAPIIT.class);
	private static final String REST_TEST_USER = "api_test_user";
	private static final String REST_TEST_USER_PASSWORD = REST_TEST_USER;
	private static final String PATH = "api/v1/";

	private static final String TEXT_PLAIN = "text/plain";
	private static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
	private static final String TEXT_CSV = "text/csv";

	private static final String PACKAGE_PERMISSION_ID = "package_permission_ID";
	private static final String ENTITY_TYPE_PERMISSION_ID = "entityType_permission_ID";
	private static final String ATTRIBUTE_PERMISSION_ID = "attribute_permission_ID";

	private static final String TYPE_TEST_PERMISSION_ID = "typeTest_permission_ID";
	private static final String TYPE_TEST_REF_PERMISSION_ID = "typeTestRef_permission_ID";
	private static final String LOCATION_PERMISSION_ID = "location_permission_ID";
	private static final String PERSONS_PERMISSION_ID = "persons_permission_ID";

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

		LOG.info("Importing RestControllerV1_TestEMX.xlsx...");
		uploadEMX(adminToken, "/RestControllerV1_TestEMX.xlsx");
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, PATH, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);

		grantSystemRights(adminToken, PATH, PACKAGE_PERMISSION_ID, testUserId, "sys_md_Package", RestControllerIT.Permission.WRITE);
		grantSystemRights(adminToken, PATH, ENTITY_TYPE_PERMISSION_ID, testUserId, "sys_md_EntityType", RestControllerIT.Permission.WRITE);
		grantSystemRights(adminToken, PATH, ATTRIBUTE_PERMISSION_ID, testUserId, "sys_md_Attribute", RestControllerIT.Permission.WRITE);

		grantSystemRights(adminToken, PATH, "sys_sec_UserAuthority_ID", testUserId, "sys_sec_UserAuthority", RestControllerIT.Permission.COUNT);
		grantSystemRights(adminToken, PATH, "sys_FileMeta_ID", testUserId, "sys_FileMeta", RestControllerIT.Permission.WRITEMETA);


		grantRights(adminToken, PATH, TYPE_TEST_PERMISSION_ID, testUserId, "TypeTest", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, PATH, TYPE_TEST_REF_PERMISSION_ID, testUserId, "TypeTestRef", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, PATH, LOCATION_PERMISSION_ID, testUserId, "Location", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, PATH, PERSONS_PERMISSION_ID, testUserId, "Person", RestControllerIT.Permission.WRITE);

		// Add home plugin
		// Add Language entity

		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

		@Test
		public void testEntityExists()
		{
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_PLAIN).when()
					.get(PATH + "it_emx_datatypes_TypeTestRef/exist").then().log().all().statusCode(200)
					.body(equalTo("true"));
		}

		@Test
		public void testEntityNotExists()
		{
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_PLAIN).when()
					.get(PATH + "sys_NonExistingEntity/exist").then().log().all().statusCode(200).body(equalTo("false"));
		}

		@Test
		public void testGetEntityType()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef/meta").then().log()
					.all();
			validateGetEntityType(response);
		}

		@Test
		public void testGetEntityTypePost()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
					.post(PATH + "it_emx_datatypes_TypeTestRef/meta?_method=GET").then().log().all();
			validateGetEntityType(response);
		}

		@Test
		public void testRetrieveEntityAttributeMeta()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).when().get(PATH + "sys_scr_ScriptType/meta/name").then().log().all();
			validateRetrieveEntityAttributeMeta(response);
		}

		@Test
		public void testRetrieveEntityAttributeMetaPost()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
					.post(PATH + "sys_scr_ScriptType/meta/name?_method=GET").then().log().all();
			validateRetrieveEntityAttributeMeta(response);
		}

		@Test
		public void testRetrieveEntity()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef/ref1").then().log()
					.all();
			validateRetrieveEntity(response);
		}

		@Test
		public void testRetrieveEntityPost()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).body(new EntityTypeRequest()).when()
					.post(PATH + "it_emx_datatypes_TypeTestRef/ref1?_method=GET").then().log().all();
			validateRetrieveEntity(response);
		}

		@Test
		public void testRetrieveEntityAttribute()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTest/1/xxref_value").then().log()
					.all();
			validateRetrieveEntityAttribute(response);
		}

		@Test
		public void testRetrieveEntityAttributePost()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).body(new EntityCollectionRequest()).when()
					.post(PATH + "it_emx_datatypes_TypeTest/1/xxref_value?_method=GET").then().log().all();
			validateRetrieveEntityAttribute(response);
		}

		@Test
		public void testRetrieveEntityCollectionResponse()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).when().get(PATH + "it_emx_datatypes_TypeTestRef").then().log().all();
			validateRetrieveEntityCollectionResponse(response);
		}

		@Test
		public void testRetrieveEntityCollectionResponsePost()
		{
			ValidatableResponse response = given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.contentType(APPLICATION_JSON).body(new EntityCollectionRequest()).when()
					.post(PATH + "it_emx_datatypes_TypeTestRef?_method=GET").then().log().all();
			validateRetrieveEntityCollectionResponse(response);
		}

		@Test(enabled = false)
		// FIXME valdate output in CSV
		public void testRetrieveEntityCollection()
		{
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_CSV).when()
					.get(PATH + "csv/it_emx_datatypes_TypeTestRef").then().contentType(TEXT_CSV).log().all().statusCode(200)
					.body(equalTo(""));
		}

		@Test
		public void testCreateFromFormPost()
		{
			// Add new entity from form post
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_FORM_URL_ENCODED)
					.formParam("value", "ref6").formParam("label", "label6").when()
					.post(PATH + "it_emx_datatypes_TypeTestRef").then().log().all().statusCode(201);

			// Check if entity was added
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
					.get(PATH + "it_emx_datatypes_TypeTestRef/ref6").then().log().all().statusCode(200)
					.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref6"), "value", equalTo("ref6"), "label",
							equalTo("label6"));
		}

		@Test(enabled = false)
		// FIXME 500 error
		public void testCreateFromFormPostMultiPart()
		{
			// Add new entity from multipart form post
			given().log().all().config(config()
					.encoderConfig(encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.JSON)))
					.header(X_MOLGENIS_TOKEN, this.testUserToken).contentType("multipart/form-data")
					.formParam("name", "IT_ScriptType").when().post(PATH + "sys_scr_ScriptType").then().log().all()
					.statusCode(201);

			// Check if entity was added
			String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType/IT_ScriptType\",\"name\":\"IT_ScriptType\"}";
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
					.get(PATH + "sys_scr_ScriptType/IT_ScriptType").then().log().all().statusCode(200)
					.body(equalTo(responseBody));

			// Remove entity
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.delete("api/v2/sys_scr_ScriptType/IT_ScriptType").then().log().all().statusCode(204);
		}

		@Test(enabled = false)
		// FIXME com.fasterxml.jackson.databind.JsonMappingException: No serializer found for class java.io.ByteArrayOutputStream and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) ) (through reference chain: org.springframework.mock.web.MockHttpServletResponse["outputStream"]->org.springframework.mock.web.ResponseServletOutputStream["targetStream"])
		public void testCreate()
		{
			Map<String, Object> entityMap = newHashMap();
			entityMap.put("name", "IT_ScriptType");

			// Add new entity from multipart form post
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).body(entityMap)
					.formParam("response", new MockHttpServletResponse()).when().post(PATH + "sys_scr_ScriptType").then()
					.log().all().statusCode(201);

			// Check if entity was added
			String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType/IT_ScriptType\",\"name\":\"IT_ScriptType\"}";
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
					.get(PATH + "sys_scr_ScriptType/IT_ScriptType").then().log().all().statusCode(200)
					.body(equalTo(responseBody));

			// Remove entity
			given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken)
					.delete("api/v2/sys_scr_ScriptType/IT_ScriptType").then().log().all().statusCode(204);
		}

	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntity(adminToken, "it_emx_datatypes_TypeTest");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRef");
		removeEntity(adminToken, "it_emx_datatypes_Location");
		removeEntity(adminToken, "it_emx_datatypes_Person");

		// Clean up permissions
		removeRight(adminToken, TYPE_TEST_PERMISSION_ID);
		removeRight(adminToken, TYPE_TEST_REF_PERMISSION_ID);
		removeRight(adminToken, LOCATION_PERMISSION_ID);
		removeRight(adminToken, PERSONS_PERMISSION_ID);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, this.testUserToken)
				.when().post(PATH + "logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken)
				.when().delete("api/v1/sys_sec_User/" + this.testUserId);
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
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/meta/name"), "fieldType", equalTo("STRING"), "name",
				equalTo("name"), "label", equalTo("name"), "attributes", equalTo(newArrayList()), "enumOptions",
				equalTo(newArrayList()), "maxLength", equalTo(255), "auto", equalTo(false), "nillable", equalTo(false),
				"readOnly", equalTo(true), "labelAttribute", equalTo(true), "unique", equalTo(true), "visible",
				equalTo(true), "lookupAttribute", equalTo(false), "isAggregatable", equalTo(false));
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
		response.body("href", equalTo("/api/v1/it_emx_datatypes_TypeTestRef/1/xxref_value"), "value", equalTo("ref1"),
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
				"start", equalTo(0), "num", equalTo(100), "total", equalTo(6), "items[0].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref6"), "items[0].value", equalTo("ref6"),
				"items[0].label", equalTo("label6"), "items[1].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref1"), "items[1].value", equalTo("ref1"),
				"items[1].label", equalTo("label1"), "items[2].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref2"), "items[2].value", equalTo("ref2"),
				"items[2].label", equalTo("label2"), "items[3].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref3"), "items[3].value", equalTo("ref3"),
				"items[3].label", equalTo("label3"), "items[4].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref4"), "items[4].value", equalTo("ref4"),
				"items[4].label", equalTo("label4"), "items[5].href",
				equalTo("/api/v1/it_emx_datatypes_TypeTestRef/ref5"), "items[5].value", equalTo("ref5"),
				"items[5].label", equalTo("label5"));
	}
}
