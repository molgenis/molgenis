package org.molgenis.data.rest;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.convert.RestTestUtils.*;

public class RestControllerIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);
	// Request parameters1
	private static final String PATH = "api/v1/";
	// Permission identifiers
	private static final String FREEMARKER_TEMPLATE_PERMISSION_ID = "freemarkerTemplate_permission_ID";
	private static final String SCRIPT_TYPE_PERMISSION_ID = "scriptType_permission_ID";
	private static final String SYS_SEC_USER_AUTHORITY_ID = "sys_sec_UserAuthority_ID";
	private static final String SYS_FILE_META_ID = "sys_FileMeta_ID";
	// User credentials
	private static final String REST_TEST_USER = "rest_test_user";
	private static final String REST_TEST_USER_PASSWORD = "rest_test_user_password";
	private String testUserId;
	private String testUserToken;
	private String adminToken;

	/**
	 * Pass down system properties via the mvn commandline argument
	 * <p>
	 * example:
	 * mvn test -Dtest="RestControllerIT" -DREST_TEST_HOST="https://molgenis01.gcc.rug.nl" -DREST_TEST_ADMIN_NAME="admin" -DREST_TEST_ADMIN_PW="admin"
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

		adminToken = login(adminUserName, adminPassword);

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);

		grantSystemRights(adminToken, FREEMARKER_TEMPLATE_PERMISSION_ID, testUserId, "sys_FreemarkerTemplate",
				Permission.WRITE);
		grantSystemRights(adminToken, SCRIPT_TYPE_PERMISSION_ID, testUserId, "sys_scr_ScriptType",
				Permission.READ);
		grantSystemRights(adminToken, SYS_SEC_USER_AUTHORITY_ID, testUserId, "sys_sec_UserAuthority",
				Permission.COUNT);
		grantSystemRights(adminToken, SYS_FILE_META_ID, testUserId, "sys_FileMeta", Permission.WRITEMETA);

		// Add home plugin
		// Add Language entity

		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void getWithoutTokenNotAllowed()
	{
		ValidatableResponse response;

		response = getWithoutToken("sys_FreemarkerTemplate");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_FreemarkerTemplate]"));

		response = getWithoutToken("sys_scr_ScriptType");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_scr_ScriptType]"));

		response = getWithoutToken("sys_sec_UserAuthority");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_sec_UserAuthority]"));

		response = getWithoutToken("sys_sec_GroupAuthority");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_sec_GroupAuthority]"));
	}

	@Test
	public void getWithTokenIsAllowed()
	{
		getWithToken("sys_FreemarkerTemplate", this.testUserToken).log().body().statusCode(200);
		getWithToken("sys_scr_ScriptType", this.testUserToken).log().body().statusCode(200);
	}

	@Test
	public void getWithoutReadPermissionIsNotAllowed()
	{
		ValidatableResponse response = getWithToken("sys_sec_UserAuthority", this.testUserToken);
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [READ] permission on entity [sys_sec_UserAuthority]"));
	}

	@Test
	public void getWithoutCountPermissionIsNotAllowed()
	{
		ValidatableResponse response = getWithToken("sys_sec_GroupAuthority", this.testUserToken);
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_sec_GroupAuthority]"));
	}

	@Test
	public void deleteNonExistingEntity()
	{
		given().log().uri().header(X_MOLGENIS_TOKEN, this.testUserToken).when()
				.delete(PATH + "sys_FileMeta" + "/non-existing-entity_id").then().log().all().statusCode(NOT_FOUND)
				.body("errors.message[0]", equalTo("Unknown [File metadata] with id [non-existing-entity_id]"));
	}

	@Test
	public void deleteWithoutWritePermissionFails()
	{
		given().log().method().log().uri().header(X_MOLGENIS_TOKEN, this.testUserToken).when()
				.delete(PATH + "sys_scr_ScriptType/R").then().statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [WRITE] permission on entity [sys_scr_ScriptType]"));

		given().log().method().log().uri().header(X_MOLGENIS_TOKEN, this.testUserToken).when()
				.delete(PATH + "sys_sec_UserAuthority").then().statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [WRITE] permission on entity [sys_sec_UserAuthority]"));

		given().log().method().log().uri().header(X_MOLGENIS_TOKEN, this.testUserToken).when()
				.delete(PATH + "sys_sec_GroupAuthority").then().statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [WRITE] permission on entity [sys_sec_GroupAuthority]"));
	}

	@Test
	public void logoutWithoutTokenFails()
	{
		given().log().uri().log().method().when().post(PATH + "logout").then().statusCode(BAD_REQUEST).log().all()
				.body("errors.message[0]", equalTo("Missing token in header"));
	}

	@Test
	public void logoutWithToken()
	{
		given().log().uri().log().method().header(X_MOLGENIS_TOKEN, this.testUserToken).when().post(PATH + "logout")
				.then().statusCode(OKE).log().all();

		given().log().uri().log().method().header(X_MOLGENIS_TOKEN, this.testUserToken).when()
				.get(PATH + "sys_FreemarkerTemplate").then().statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_FreemarkerTemplate]"));

		given().log().uri().log().method().when().get(PATH + "sys_FreemarkerTemplate").then().statusCode(UNAUTHORIZED)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [sys_FreemarkerTemplate]"));

		// clean up after test
		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	private ValidatableResponse getWithoutToken(String requestedEntity)
	{
		return given().log().uri().when().get(PATH + requestedEntity).then();
	}

	private ValidatableResponse getWithToken(String requestedEntity, String token)
	{
		return given().log().all().header(X_MOLGENIS_TOKEN, token).contentType(APPLICATION_JSON).when()
				.get(PATH + requestedEntity).then();

	}

	@AfterClass
	public void afterClass()
	{
		// Clean up permissions
		removeRight(adminToken, FREEMARKER_TEMPLATE_PERMISSION_ID);
		removeRight(adminToken, SCRIPT_TYPE_PERMISSION_ID);
		removeRight(adminToken, SYS_FILE_META_ID);
		removeRight(adminToken, SYS_SEC_USER_AUTHORITY_ID);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, this.testUserToken).when().post(PATH + "logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}

	public enum Permission
	{
		READ, WRITE, COUNT, NONE, WRITEMETA;
	}

}

