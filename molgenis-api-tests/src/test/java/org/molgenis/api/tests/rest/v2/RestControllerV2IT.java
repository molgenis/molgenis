package org.molgenis.api.tests.rest.v2;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.molgenis.api.tests.rest.v1.RestControllerIT;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.security.auth.UserMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.*;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;

public class RestControllerV2IT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	private static final String API_V2 = "api/v2/";

	private static final String REST_TEST_USER_PASSWORD = "Blahdiblah";

	private String testUserName;
	private String testUserToken;
	private String adminToken;
	private String testUserId;

	private List<String> testEntities = newArrayList("it_emx_datatypes_TypeTestv2", "it_emx_datatypes_TypeTestRefv2",
			"it_emx_datatypes_Locationv2", "it_emx_datatypes_Personv2");

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
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Clean up test entities if they already exist...");
		removeEntities(adminToken, testEntities);
		LOG.info("Cleaned up existing test entities.");

		LOG.info("Importing RestControllerV2_TestEMX.xlsx...");
		uploadEMX(adminToken, "/RestControllerV2_TestEMX.xlsx");
		LOG.info("Importing Done");

		testUserName = "rest_test_v2" + System.currentTimeMillis();
		createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD);
		testUserId = getUserId(adminToken, testUserName);

		ImmutableMap.Builder<String, Permission> permissionsBuilder = ImmutableMap.builder();
		permissionsBuilder.put(PACKAGE, WRITE)
						  .put(ENTITY_TYPE_META_DATA, WRITE)
						  .put(ATTRIBUTE_META_DATA, WRITE)
						  .put(FILE_META, READ)
						  .put(UserMetaData.USER, COUNT);
		testEntities.forEach(entity -> permissionsBuilder.put(entity, WRITE));
		setGrantedRepositoryPermissions(adminToken, testUserId, permissionsBuilder.build());

		testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void testApiCorsPreflightRequest()
	{
		given().log()
			   .all()
			   .header("Access-Control-Request-Method", "DELETE ")
			   .header("Access-Control-Request-Headers", "x-molgenis-token")
			   .header("Origin", "https://foo.bar.org")
			   .when()
			   .options(API_V2 + "version")
			   .then()
			   .statusCode(OKE)
			   .log()
			   .all()
			   .header("Access-Control-Allow-Origin", "*")
			   .header("Access-Control-Allow-Methods", "DELETE")
			   .header("Access-Control-Allow-Headers", "x-molgenis-token")
			   .header("Access-Control-Max-Age", "1800");
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

		given().log()
			   .method()
			   .log()
			   .uri()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(jsonObject.toJSONString())
			   .when()
			   .post(API_V2 + "it_emx_datatypes_TypeTestRefv2")
			   .then()
			   .statusCode(RestTestUtils.CREATED)
			   .log()
			   .all()
			   .body("location",
					   Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestRefv2?q=value=in=(\"ref55\",\"ref57\")"));
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

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(jsonObject.toJSONString())
			   .when()
			   .post(API_V2 + "it_emx_datatypes_Locationv2")
			   .then()
			   .statusCode(RestTestUtils.CREATED)
			   .log()
			   .all()
			   .body("location", Matchers.equalTo(expectedLocation), "resources[0].href",
					   Matchers.equalTo(expectedHref));

	}

	@Test(dependsOnMethods = "batchCreate")
	public void batchCreateTypeTest()
	{

		JSONObject entities = readJsonFile("/createEntitiesv2.json");

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(entities.toJSONString())
			   .when()
			   .post(API_V2 + "it_emx_datatypes_TypeTestv2")
			   .then()
			   .statusCode(RestTestUtils.CREATED)
			   .log()
			   .all()
			   .body("location", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2?q=id=in=(\"55\",\"57\")"),
					   "resources[0].href", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/55"),
					   "resources[1].href", Matchers.equalTo("/api/v2/it_emx_datatypes_TypeTestv2/57"));

	}

	@Test(dependsOnMethods = "batchCreateTypeTest", priority = 3)
	public void batchUpdate()
	{
		JSONObject entities = readJsonFile("/updateEntitiesv2.json");

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(entities.toJSONString())
			   .when()
			   .put(API_V2 + "it_emx_datatypes_TypeTestv2")
			   .then()
			   .statusCode(OKE)
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

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(jsonObject.toJSONString())
			   .when()
			   .put(API_V2 + "it_emx_datatypes_TypeTestv2/xdatetime")
			   .then()
			   .statusCode(OKE)
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

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(jsonObject.toJSONString())
			   .when()
			   .delete(API_V2 + "it_emx_datatypes_TypeTestv2")
			   .then()
			   .statusCode(NO_CONTENT)
			   .log()
			   .all();
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityCollectionAggregatesNotAllowed()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .when()
			   .get(API_V2 + "sys_App?aggs=x==active;y==superuser")
			   .then()
			   .statusCode(UNAUTHORIZED);
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityCollectionAggregates()
	{

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .when()
			   .get(API_V2 + "sys_sec_User?aggs=x==active;y==superuser;distinct==active")
			   .then()
			   .statusCode(OKE)
			   .body("aggs.matrix[0][0]", Matchers.equalTo(1));
	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntities(adminToken, testEntities);

		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		cleanupUserToken(testUserToken);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}
}
