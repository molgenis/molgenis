package org.molgenis.controller.api.tests.rest.v1;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.molgenis.controller.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.molgenis.controller.api.tests.utils.RestTestUtils.Permission;

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
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + RestAssured.baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? RestTestUtils.DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = RestTestUtils.login(adminUserName, adminPassword);

		RestTestUtils.createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = RestTestUtils.getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_FreemarkerTemplate", Permission.WRITE);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_scr_ScriptType", Permission.READ);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_sec_UserAuthority", Permission.COUNT);
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_FileMeta", Permission.WRITEMETA);

		this.testUserToken = RestTestUtils.login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	@Test
	public void getWithoutTokenNotAllowed()
	{
		ValidatableResponse response;

		response = getWithoutToken("sys_FreemarkerTemplate");
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		response = getWithoutToken("sys_scr_ScriptType");
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No read permission on entity type 'Script type' with id 'sys_scr_ScriptType'"));

		response = getWithoutToken("sys_sec_UserAuthority");
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No read permission on entity type 'User authority' with id 'sys_sec_UserAuthority'"));

		response = getWithoutToken("sys_sec_GroupAuthority");
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No read permission on entity type 'Group authority' with id 'sys_sec_GroupAuthority'"));
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
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No [READ] permission on entity type [User authority] with id [sys_sec_UserAuthority]"));
	}

	@Test
	public void getWithoutCountPermissionIsNotAllowed()
	{
		ValidatableResponse response = getWithToken("sys_sec_GroupAuthority", this.testUserToken);
		response.statusCode(RestTestUtils.UNAUTHORIZED)
				.body("errors.message[0]",
						Matchers.equalTo("No read permission on entity type 'Group authority' with id 'sys_sec_GroupAuthority'"));
	}

	@Test
	public void deleteNonExistingEntity()
	{
		RestAssured.given().log()
				   .uri()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .delete(PATH + "sys_FileMeta" + "/non-existing-entity_id")
				   .then()
				   .log()
				   .all()
				   .statusCode(RestTestUtils.NOT_FOUND)
				   .body("errors.message[0]", Matchers.equalTo("Unknown [File metadata] with id [non-existing-entity_id]"));
	}

	@Test
	public void deleteWithoutWritePermissionFails()
	{
		RestAssured.given().log()
				   .method()
				   .log()
				   .uri()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .delete(PATH + "sys_scr_ScriptType/R")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]",
					   Matchers.equalTo("No [WRITE] permission on entity type [Script type] with id [sys_scr_ScriptType]"));

		RestAssured.given().log()
				   .method()
				   .log()
				   .uri()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .delete(PATH + "sys_sec_UserAuthority")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]",
					   Matchers.equalTo("No [WRITE] permission on entity type [User authority] with id [sys_sec_UserAuthority]"));

		RestAssured.given().log()
				   .method()
				   .log()
				   .uri()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .delete(PATH + "sys_sec_GroupAuthority")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]",
					   Matchers.equalTo("No read permission on entity type 'Group authority' with id 'sys_sec_GroupAuthority'"));
	}

	@Test
	public void logoutWithoutTokenFails()
	{
		RestAssured.given().log()
				   .uri()
				   .log()
				   .method()
				   .when()
				   .post(PATH + "logout")
				   .then()
				   .statusCode(RestTestUtils.BAD_REQUEST)
				   .log()
				   .all()
				   .body("errors.message[0]", Matchers.equalTo("Missing token in header"));
	}

	@Test
	public void logoutWithToken()
	{
		RestAssured.given().log()
				   .uri()
				   .log()
				   .method()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .post(PATH + "logout")
				   .then()
				   .statusCode(RestTestUtils.OKE)
				   .log()
				   .all();

		RestAssured.given().log()
				   .uri()
				   .log()
				   .method()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(PATH + "sys_FreemarkerTemplate")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]",
					   Matchers.equalTo("No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		RestAssured.given().log()
				   .uri()
				   .log()
				   .method()
				   .when()
				   .get(PATH + "sys_FreemarkerTemplate")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]",
					   Matchers.equalTo("No read permission on entity type 'Freemarker template' with id 'sys_FreemarkerTemplate'"));

		// clean up after test
		this.testUserToken = RestTestUtils.login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6575
	@Test
	public void testRetrieveResourceWithFileExtensionIdNotFound()
	{
		RestAssured.given().log()
				   .uri()
				   .log()
				   .method()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(PATH + "sys_FreemarkerTemplate/test.csv")
				   .then()
				   .statusCode(RestTestUtils.NOT_FOUND)
				   .body("errors.message[0]", Matchers.equalTo("sys_FreemarkerTemplate test.csv not found"));
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test
	public void testRetrieveSystemEntityTypeNotAllowed()
	{
		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(PATH + "sys_sec_User/meta")
				   .then()
				   .statusCode(RestTestUtils.UNAUTHORIZED)
				   .body("errors.message[0]", Matchers.equalTo("No read permission on entity type 'User' with id 'sys_sec_User'"));
	}

	// Regression test for https://github.com/molgenis/molgenis/issues/6731
	@Test(dependsOnMethods = { "testRetrieveSystemEntityTypeNotAllowed" })
	public void testRetrieveSystemEntityType()
	{
		RestTestUtils.grantSystemRights(adminToken, testUserId, "sys_sec_User", Permission.COUNT);

		RestAssured.given().log()
				   .all()
				   .header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken)
				   .when()
				   .get(PATH + "sys_sec_User/meta")
				   .then()
				   .statusCode(RestTestUtils.OKE)
				   .body("name", Matchers.equalTo("sys_sec_User"));
	}

	private ValidatableResponse getWithoutToken(String requestedEntity)
	{
		return RestAssured.given().log().uri().when().get(PATH + requestedEntity).then();
	}

	private ValidatableResponse getWithToken(String requestedEntity, String token)
	{
		return RestAssured.given().log()
						  .all()
						  .header(RestTestUtils.X_MOLGENIS_TOKEN, token)
						  .contentType(RestTestUtils.APPLICATION_JSON)
						  .when()
						  .get(PATH + requestedEntity)
						  .then();

	}

	@AfterClass
	public void afterClass()
	{
		// Clean up permissions
		RestTestUtils.removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		RestAssured.given().header(RestTestUtils.X_MOLGENIS_TOKEN, this.testUserToken).when().post("api/v1/logout");

		// Clean up user
		RestAssured.given().header(RestTestUtils.X_MOLGENIS_TOKEN, this.adminToken).when().delete("api/v1/sys_sec_User/" + this.testUserId);
	}

}