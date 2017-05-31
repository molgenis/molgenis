package org.molgenis.data.rest.v2;

import io.restassured.RestAssured;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.elasticsearch.common.Strings;
import org.molgenis.data.rest.RestControllerIT;
import org.molgenis.data.rest.convert.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.convert.RestTestUtils.*;
import static org.molgenis.data.rest.convert.RestTestUtils.Permission.READ;
import static org.molgenis.data.rest.convert.RestTestUtils.Permission.WRITE;

public class RestControllerV2IT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	private static final String API_V2 = "api/v2/";

	private static final String REST_TEST_USER = "rest_test_v2";
	private static final String REST_TEST_USER_PASSWORD = REST_TEST_USER;

	private String testUserToken;
	private String adminToken;
	private String testUserId;

	/**
	 * Pass down system properties via the mvn commandline argument
	 * example:
	 * mvn test -Dtest="RestControllerV2IT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
	 */
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

		adminToken = RestTestUtils.login(adminUserName, adminPassword);

		LOG.info("Clean up test entities if they already exist...");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestv2");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRefv2");
		removeEntity(adminToken, "it_emx_datatypes_Locationv2");
		removeEntity(adminToken, "it_emx_datatypes_Personv2");
		LOG.info("Cleaned up existing test entities.");

		LOG.info("Importing RestControllerV2_TestEMX.xlsx...");
		uploadEMX(adminToken, "/RestControllerV2_TestEMX.xlsx");
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		String adminUserId = getUserId(adminToken, adminUserName);
		LOG.info("testUserId: " + testUserId);
		LOG.info("adminUserId " + adminUserId);

		grantSystemRights(adminToken, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_Attribute", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_FileMeta", READ);
		grantSystemRights(adminToken, testUserId, "sys_sec_Owned", READ);

		grantRights(adminToken, testUserId, "it_emx_datatypes_TypeTestv2", WRITE);
		grantRights(adminToken, testUserId, "it_emx_datatypes_TypeTestRefv2", WRITE);
		grantRights(adminToken, testUserId, "it_emx_datatypes_Locationv2", WRITE);
		grantRights(adminToken, testUserId, "it_emx_datatypes_Personv2", WRITE);

		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
		LOG.info("Test user token:" + this.testUserToken);
	}

	@Test
	public void batchCreate()
	{
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

		given().log().method().log().uri().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString()).when().post(API_V2 + "it_emx_datatypes_TypeTestRefv2").then()
				.statusCode(CREATED).log().all()
				.body("location", equalTo("/api/v2/it_emx_datatypes_TypeTestRefv2?q=value=in=(\"ref55\",\"ref57\")"));
	}

	@Test
	public void batchCreateLocation()
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray entities = new JSONArray();

		JSONObject entity = new JSONObject();
		entity.put("Chromosome", "42");
		entity.put("Position", 42);
		entities.add(entity);

		jsonObject.put("entities", entities);

		String expectedLocation = "/api/v2/it_emx_datatypes_Locationv2?q=Position=in=(\"42\")";
		String expectedHref = "/api/v2/it_emx_datatypes_Locationv2/42";

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString()).when().post(API_V2 + "it_emx_datatypes_Locationv2").then()
				.statusCode(CREATED).log().all()
				.body("location", equalTo(expectedLocation), "resources[0].href", equalTo(expectedHref));

	}

	@Test(dependsOnMethods = "batchCreate")
	public void batchCreateTypeTest()
	{

		JSONObject entities = readJsonFile("/createEntitiesv2.json");

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(entities.toJSONString()).when().post(API_V2 + "it_emx_datatypes_TypeTestv2").then()
				.statusCode(CREATED).log().all()
				.body("location", equalTo("/api/v2/it_emx_datatypes_TypeTestv2?q=id=in=(\"55\",\"57\")"),
						"resources[0].href", equalTo("/api/v2/it_emx_datatypes_TypeTestv2/55"), "resources[1].href",
						equalTo("/api/v2/it_emx_datatypes_TypeTestv2/57"));

	}

	@Test(dependsOnMethods = "batchCreateTypeTest", priority = 3)
	public void batchUpdate()
	{
		JSONObject entities = readJsonFile("/updateEntitiesv2.json");

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(entities.toJSONString()).when().put(API_V2 + "it_emx_datatypes_TypeTestv2").then().statusCode(OKE)
				.log().all();
	}

	@Test(dependsOnMethods = { "batchCreate", "batchCreateTypeTest", "batchUpdate" }, priority = 5)
	public void batchUpdateOnlyOneAttribute()
	{
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

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString()).when().put(API_V2 + "it_emx_datatypes_TypeTestv2/xdatetime").then()
				.statusCode(OKE).log().all();
	}

	@Test(dependsOnMethods = { "batchCreate", "batchCreateTypeTest", "batchUpdate" }, priority = 10)
	public void batchDelete()
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray entityIds = new JSONArray();
		entityIds.add("55");
		entityIds.add("57");
		jsonObject.put("entityIds", entityIds);

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString()).when().delete(API_V2 + "it_emx_datatypes_TypeTestv2").then()
				.statusCode(NO_CONTENT).log().all();
	}

	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntity(adminToken, "it_emx_datatypes_TypeTestv2");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRefv2");
		removeEntity(adminToken, "it_emx_datatypes_Locationv2");
		removeEntity(adminToken, "it_emx_datatypes_Personv2");

		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, this.testUserToken).when().post("api/v1/logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}
}
