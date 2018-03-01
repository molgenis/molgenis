package org.molgenis.api.tests.rest.v1;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.data.security.auth.UserMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.*;

public class RestControllerIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	// Request parameters
	private static final String PATH = "api/v1/";

	// User credentials
	private String testUserName;
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
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		testUserName = "rest_test_user" + System.currentTimeMillis();
		createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD);
		testUserId = getUserId(adminToken, testUserName);

		setGrantedRepositoryPermissions(adminToken, testUserId,
				ImmutableMap.<String, Permission>builder().put("sys_FreemarkerTemplate", WRITE)
														  .put("sys_scr_ScriptType", READ)
														  .put("sys_FileMeta", WRITEMETA)
														  .put(UserMetaData.USER, COUNT)
														  .build());

		testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void getWithoutTokenNotAllowed()
	{
		ValidatableResponse response;

		response = getWithoutToken("sys_FreemarkerTemplate");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", Matchers.equalTo(
						"No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		response = getWithoutToken("sys_scr_ScriptType");
		response.statusCode(UNAUTHORIZED)
				.body("errors.message[0]", Matchers.equalTo(
						"No read permission on entity type 'Script type' with id 'sys_scr_ScriptType'"));
	}

	@Test
	public void getWithTokenIsAllowed()
	{
		getWithToken("sys_FreemarkerTemplate", this.testUserToken).log().body().statusCode(200);
		getWithToken("sys_scr_ScriptType", this.testUserToken).log().body().statusCode(200);
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
			   .all().statusCode(NO_CONTENT);
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
			   .body("errors.message[0]", Matchers.equalTo(
					   "No [WRITE] permission on entity type [Script type] with id [sys_scr_ScriptType]"));
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
			   .all();
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
			   .statusCode(RestTestUtils.OKE)
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
			   .body("errors.message[0]", Matchers.equalTo(
					   "No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		given().log()
			   .uri()
			   .log()
			   .method()
			   .when()
			   .get(PATH + "sys_FreemarkerTemplate")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]", Matchers.equalTo(
					   "No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		// clean up after test
		this.testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6575
	@Test
	public void testRetrieveResourceWithFileExtensionIdNotFound()
	{
		given().log()
			   .uri()
			   .log()
			   .method()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .get(PATH + "sys_FreemarkerTemplate/test.csv")
			   .then()
			   .statusCode(NOT_FOUND)
			   .body("errors.message[0]", Matchers.equalTo("sys_FreemarkerTemplate test.csv not found"));
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityTypeNotAllowed()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .get(PATH + "sys_App/meta")
			   .then()
			   .statusCode(UNAUTHORIZED)
			   .body("errors.message[0]",
					   Matchers.equalTo("No read permission on entity type 'App' with id 'sys_App'"));
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityType()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, this.testUserToken)
			   .when()
			   .get(PATH + "sys_sec_User/meta")
			   .then()
			   .statusCode(RestTestUtils.OKE)
			   .body("name", Matchers.equalTo("sys_sec_User"));
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

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		cleanupUserToken(testUserToken);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}

}