package org.molgenis.data.rest;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.molgenis.data.rest.convert.RestTestUtils;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.convert.RestTestUtils.*;

public class RestControllerIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	// Request parameters
	private static final String PATH = "api/v1/";

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
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);
		grantSystemRights(adminToken, testUserId, "sys_FreemarkerTemplate", Permission.WRITE);
		grantSystemRights(adminToken, testUserId, "sys_scr_ScriptType", Permission.READ);
		grantSystemRights(adminToken, testUserId, "sys_sec_UserAuthority", Permission.COUNT);
		grantSystemRights(adminToken, testUserId, "sys_FileMeta", Permission.WRITEMETA);

		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void getWithoutTokenNotAllowed()
	{
		ValidatableResponse response;

		response = getWithoutToken("sys_FreemarkerTemplate");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]",
						equalTo("No [COUNT] permission on entity type [Freemarker template] with id [sys_FreemarkerTemplate]"));

		response = getWithoutToken("sys_scr_ScriptType");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]",
						equalTo("No [COUNT] permission on entity type [Script type] with id [sys_scr_ScriptType]"));

		response = getWithoutToken("sys_sec_UserAuthority");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]",
						equalTo("No [COUNT] permission on entity type [User authority] with id [sys_sec_UserAuthority]"));

		response = getWithoutToken("sys_sec_GroupAuthority");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]",
						equalTo("No [COUNT] permission on entity type [Group authority] with id [sys_sec_GroupAuthority]"));
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
				.body("errors.message[0]",
						equalTo("No [READ] permission on entity type [User authority] with id [sys_sec_UserAuthority]"));
	}

	@Test
	public void getWithoutCountPermissionIsNotAllowed()
	{
		ValidatableResponse response = getWithToken("sys_sec_GroupAuthority", this.testUserToken);
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]",
						equalTo("No [COUNT] permission on entity type [Group authority] with id [sys_sec_GroupAuthority]"));
	}

	@Test
	public void test2faEnforced()
	{
		RestTestUtils.toggle2fa(this.adminToken, TwoFactorAuthenticationSetting.ENFORCED);

		try
		{
			ValidatableResponse response;

			response = given().log()
							  .all()
							  .header(X_MOLGENIS_TOKEN, testUserToken)
							  .contentType(APPLICATION_JSON)
							  .when()
							  .get(PATH + "logout")
							  .then();
			response.statusCode(OKE);

			Gson gson = new Gson();
			Map<String, String> loginBody = new HashMap<>();
			loginBody.put("username", REST_TEST_USER);
			loginBody.put("password", REST_TEST_USER_PASSWORD);

			response = given().contentType(APPLICATION_JSON)
							  .body(gson.toJson(loginBody))
							  .when()
							  .post(PATH + "login")
							  .then();
			response.statusCode(UNAUTHORIZED)
					.body("errors.message[0]",
							equalTo("Login using /api/v1/login is disabled, two factor authentication is enabled"));
		}
		finally
		{
			RestTestUtils.toggle2fa(this.adminToken, TwoFactorAuthenticationSetting.DISABLED);
		}
	}

	@Test
	public void test2faEnabled()
	{
		RestTestUtils.toggle2fa(this.adminToken, TwoFactorAuthenticationSetting.ENABLED);

		try
		{
			ValidatableResponse response;

			response = given().log()
							  .all()
							  .header(X_MOLGENIS_TOKEN, testUserToken)
							  .contentType(APPLICATION_JSON)
							  .when()
							  .get(PATH + "logout")
							  .then();
			response.statusCode(OKE);

			Gson gson = new Gson();
			Map<String, String> loginBody = new HashMap<>();
			loginBody.put("username", REST_TEST_USER);
			loginBody.put("password", REST_TEST_USER_PASSWORD);

			response = given().contentType(APPLICATION_JSON)
							  .body(gson.toJson(loginBody))
							  .when()
							  .post(PATH + "login")
							  .then();
			response.statusCode(OKE);
		}
		finally
		{
			RestTestUtils.toggle2fa(this.adminToken, TwoFactorAuthenticationSetting.DISABLED);
		}
	}

	@Test
	public void deleteNonExistingEntity()
	{
		given().log()
			   .uri()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .delete(PATH + "sys_FileMeta" + "/non-existing-entity_id")
			   .then()
			   .log()
			   .all()
			   .statusCode(NOT_FOUND)
			   .body("errors.message[0]", equalTo("Unknown [File metadata] with id [non-existing-entity_id]"));
	}

	@Test
	public void deleteWithoutWritePermissionFails()
	{
		given().log()
			   .method()
			   .log()
			   .uri()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .delete(PATH + "sys_scr_ScriptType/R")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   equalTo("No [WRITE] permission on entity type [Script type] with id [sys_scr_ScriptType]"));

		given().log()
			   .method()
			   .log()
			   .uri()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .delete(PATH + "sys_sec_UserAuthority")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   equalTo("No [WRITE] permission on entity type [User authority] with id [sys_sec_UserAuthority]"));

		given().log()
			   .method()
			   .log()
			   .uri()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .delete(PATH + "sys_sec_GroupAuthority")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   equalTo("No [WRITE] permission on entity type [Group authority] with id [sys_sec_GroupAuthority]"));
	}

	@Test
	public void logoutWithoutTokenFails()
	{
		given().log()
			   .uri()
			   .log()
			   .method()
			   .when()
			   .post(PATH + "logout")
			   .then()
			   .statusCode(BAD_REQUEST)
			   .log()
			   .all()
			   .body("errors.message[0]", equalTo("Missing token in header"));
	}

	@Test
	public void logoutWithToken()
	{
		given().log()
			   .uri()
			   .log()
			   .method()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .post(PATH + "logout")
			   .then()
			   .statusCode(OKE)
			   .log()
			   .all();

		given().log()
			   .uri()
			   .log()
			   .method()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .get(PATH + "sys_FreemarkerTemplate")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   equalTo("No [COUNT] permission on entity type [Freemarker template] with id [sys_FreemarkerTemplate]"));

		given().log()
			   .uri()
			   .log()
			   .method()
			   .when()
			   .get(PATH + "sys_FreemarkerTemplate")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   equalTo("No [COUNT] permission on entity type [Freemarker template] with id [sys_FreemarkerTemplate]"));

		// clean up after test
		this.testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	private ValidatableResponse getWithoutToken(String requestedEntity)
	{
		return given().log().uri().when().get(PATH + requestedEntity).then();
	}

	private ValidatableResponse getWithToken(String requestedEntity, String token)
	{
		return given().log()
					  .all()
					  .header(X_MOLGENIS_TOKEN, token)
					  .contentType(APPLICATION_JSON)
					  .when()
					  .get(PATH + requestedEntity)
					  .then();

	}

	@AfterClass
	public void afterClass()
	{
		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, this.testUserToken).when().post("api/v1/logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}

}