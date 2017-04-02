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
import static org.molgenis.data.rest.RestControllerIT.Permission.READ;
import static org.molgenis.data.rest.RestControllerIT.Permission.WRITE;
import static org.molgenis.data.rest.convert.RestTestUtils.*;

public class RestControllerV2IT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	private static final String API_V2 = "api/v2/";

	private static final String REST_TEST_USER = "rest_test_v2";
	private static final String REST_TEST_USER_PASSWORD = REST_TEST_USER;

	private static final String PACKAGE_PERMISSION_ID = "package_permission_ID";
	private static final String ENTITY_TYPE_PERMISSION_ID = "entityType_permission_ID";
	private static final String ATTRIBUTE_PERMISSION_ID = "attribute_permission_ID";
	private static final String FILE_META_PERMISSION_ID = "file_meta_permission_ID";
	private static final String OWNED_PERMISSION_ID = "owned_permission_ID";

	private static final String TYPE_TEST_PERMISSION_V2_ID = "typeTest_permissionc_v2_ID";
	private static final String TYPE_TEST_REF_PERMISSION_V2_ID = "typeTestRef_permission_v2_ID";
	private static final String LOCATION_PERMISSION_V2_ID = "location_permission_v2_ID";
	private static final String PERSONS_PERMISSION_V2_ID = "persons_permission_v2_ID";

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

		LOG.info("Importing RestControllerV2_TestEMX.xlsx...");
		uploadEMX(adminToken, "/RestControllerV2_TestEMX.xlsx");
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		String adminUserId = getUserId(adminToken, adminUserName);
		LOG.info("testUserId: " + testUserId);
		LOG.info("adminUserId " + adminUserId);


		grantSystemRights(adminToken, PACKAGE_PERMISSION_ID, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, ENTITY_TYPE_PERMISSION_ID, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, ATTRIBUTE_PERMISSION_ID, testUserId, "sys_md_Attribute", WRITE);
		grantSystemRights(adminToken, FILE_META_PERMISSION_ID, testUserId, "sys_FileMeta", READ);
		grantSystemRights(adminToken, OWNED_PERMISSION_ID, testUserId, "sys_sec_Owned", READ);

		grantRights(adminToken, TYPE_TEST_PERMISSION_V2_ID, testUserId, "TypeTestv2", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, TYPE_TEST_REF_PERMISSION_V2_ID, testUserId, "TypeTestRefv2", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, LOCATION_PERMISSION_V2_ID, testUserId, "Locationv2", RestControllerIT.Permission.WRITE);
		grantRights(adminToken, PERSONS_PERMISSION_V2_ID, testUserId, "Personv2", RestControllerIT.Permission.WRITE);

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

		given()
				.log().method().log().uri()
				.header(X_MOLGENIS_TOKEN, this.testUserToken)
				.contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString())
				.when().post(API_V2 + "it_emx_datatypes_TypeTestRefv2")
				.then().statusCode(CREATED)
				.log().all()
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

		given()
				.log().all()
				.header(X_MOLGENIS_TOKEN, this.testUserToken)
				.contentType(APPLICATION_JSON)
				.body(jsonObject.toJSONString())
				.when().post(API_V2 + "it_emx_datatypes_Locationv2")
				.then().statusCode(CREATED)
				.log().all()
				.body("location", equalTo(expectedLocation),
						"resources[0].href", equalTo(expectedHref));

	}


	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntity(adminToken, "it_emx_datatypes_TypeTestv2");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRefv2");
		removeEntity(adminToken, "it_emx_datatypes_Locationv2");
		removeEntity(adminToken, "it_emx_datatypes_Personv2");

		// / Clean up permissions
		removeRight(adminToken, TYPE_TEST_PERMISSION_V2_ID);
		removeRight(adminToken, TYPE_TEST_REF_PERMISSION_V2_ID);
		removeRight(adminToken, LOCATION_PERMISSION_V2_ID);
		removeRight(adminToken, PERSONS_PERMISSION_V2_ID);

		removeRight(adminToken, PACKAGE_PERMISSION_ID);
		removeRight(adminToken,  ENTITY_TYPE_PERMISSION_ID);
		removeRight(adminToken,  ATTRIBUTE_PERMISSION_ID);
		removeRight(adminToken,  FILE_META_PERMISSION_ID);
		removeRight(adminToken,  OWNED_PERMISSION_ID);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, this.testUserToken)
				.when().post("api/v1/logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken)
				.when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}



}
