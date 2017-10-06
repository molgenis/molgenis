package org.molgenis.controller.api.tests.rest.v2;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.molgenis.controller.api.tests.rest.v1.RestControllerIT;
import org.molgenis.controller.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.molgenis.controller.api.tests.utils.RestTestUtils.Permission;

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
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = RestTestUtils.login(adminUserName, adminPassword);

		LOG.info("Clean up test entities if they already exist...");
		removeEntities();
		LOG.info("Cleaned up existing test entities.");

		LOG.info("Importing RestControllerV2_TestEMX.xlsx...");
		RestTestUtils.uploadEMX(adminToken, "/RestControllerV2_TestEMX.xlsx");
		LOG.info("Importing Done");

		RestTestUtils.createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = RestTestUtils.getUserId(adminToken, REST_TEST_USER);
		String adminUserId = RestTestUtils.getUserId(adminToken, adminUserName);
		LOG.info("testUserId: " + testUserId);
		LOG.info("adminUserId " + adminUserId);

		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_md_Package", Permission.WRITE);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_md_EntityType", Permission.WRITE);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_md_Attribute", Permission.WRITE);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_FileMeta", Permission.READ);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_sec_Owned", Permission.READ);

		RestTestUtils.grantRights(adminToken, testUserId, "it_emx_datatypes_TypeTestv2", Permission.WRITE);
		RestTestUtils.grantRights(adminToken, testUserId, "it_emx_datatypes_TypeTestRefv2", Permission.WRITE);
		RestTestUtils.grantRights(adminToken, testUserId, "it_emx_datatypes_Locationv2", Permission.WRITE);
		RestTestUtils.grantRights(adminToken, testUserId, "it_emx_datatypes_Personv2", Permission.WRITE);

		this.testUserToken = RestTestUtils.login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
		LOG.info("Test user token:" + this.testUserToken);
	}

	private void removeEntities()
	{
		RestTestUtils.removeEntity(adminToken, "it_emx_datatypes_TypeTestv2");
		RestTestUtils.removeEntity(adminToken, "it_emx_datatypes_TypeTestRefv2");
		RestTestUtils.removeEntity(adminToken, "it_emx_datatypes_Locationv2");
		RestTestUtils.removeEntity(adminToken, "it_emx_datatypes_Personv2");
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

		RestAssured.given().log()
				   .method()
				   .log()
				   .uri()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(jsonObject.toJSONString())
				   .when()
				   .post(API_V2 + "it_emx_datatypes_TypeTestRefv2")
				   .then()
				   .statusCode(RestTestUtils.CREATED)
				   .log()
				   .all()
				   .body("location", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestRefv2?q=value=in=(\"ref55\",\"ref57\")"));
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

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(jsonObject.toJSONString())
				   .when()
				   .post(API_V2 + "it_emx_datatypes_Locationv2")
				   .then()
				   .statusCode(RestTestUtils.CREATED)
				   .log()
				   .all()
				   .body("location", Matchers.equalTo(expectedLocation), "resources[0].href", Matchers.equalTo(expectedHref));

	}

	@Test(dependsOnMethods = "batchCreate")
	public void batchCreateTypeTest()
	{

		JSONObject entities = RestTestUtils.readJsonFile("/createEntitiesv2.json");

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(entities.toJSONString())
				   .when()
				   .post(API_V2 + "it_emx_datatypes_TypeTestv2")
				   .then()
				   .statusCode(RestTestUtils.CREATED)
				   .log()
				   .all()
				   .body("location", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2?q=id=in=(\"55\",\"57\")"),
					   "resources[0].href", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/55"), "resources[1].href",
					   Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/57"));

	}

	@Test(dependsOnMethods = "batchCreateTypeTest", priority = 3)
	public void batchUpdate()
	{
		JSONObject entities = RestTestUtils.readJsonFile("/updateEntitiesv2.json");

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(entities.toJSONString())
				   .when()
				   .put(API_V2 + "it_emx_datatypes_TypeTestv2")
				   .then()
				   .statusCode(RestTestUtils.OKE)
				   .log()
				   .all();
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

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(jsonObject.toJSONString())
				   .when()
				   .put(API_V2 + "it_emx_datatypes_TypeTestv2/xdatetime")
				   .then()
				   .statusCode(RestTestUtils.OKE)
				   .log()
				   .all();
	}

	@Test(dependsOnMethods = { "batchCreate", "batchCreateTypeTest", "batchUpdate" }, priority = 10)
	public void batchDelete()
	{
		JSONObject jsonObject = new JSONObject();
		JSONArray entityIds = new JSONArray();
		entityIds.add("55");
		entityIds.add("57");
		jsonObject.put("entityIds", entityIds);

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .contentType(RestTestUtils.APPLICATION_JSON)
				   .body(jsonObject.toJSONString())
				   .when()
				   .delete(API_V2 + "it_emx_datatypes_TypeTestv2")
				   .then()
				   .statusCode(RestTestUtils.NO_CONTENT)
				   .log()
				   .all();
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityCollectionAggregatesNotAllowed()
	{
		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(API_V2 + "sys_sec_User?aggs=x==active;y==superuser")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED);
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test(dependsOnMethods = { "testRetrieveSystemEntityCollectionAggregatesNotAllowed" })
	public void testRetrieveSystemEntityCollectionAggregates()
	{
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_sec_User", Permission.COUNT);

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(API_V2 + "sys_sec_User?aggs=x==active;y==superuser")
				   .then()
				   .statusCode(RestTestUtils.OKE)
				   .body("aggs.matrix[0][0]", Matchers.equalTo(1));
	}

	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntities();

		// Clean up permissions
		RestTestUtils.removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		RestAssured.given().header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken).when().post("api/v1/logout");

		// Clean up user
		RestAssured.given().header(RestTestUtils.X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}
}
