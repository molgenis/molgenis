package org.molgenis.data.rest;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;

public class RestControllerIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	private static final String X_MOLGENIS_TOKEN = "x-molgenis-token";
	private static final String APPLICATION_JSON = "application/json";
	private static final String PATH = "api/v1/";

	private static final String DEFAULT_HOST = "https://molgenis62.gcc.rug.nl/";
	private static final String DEFAULT_ADMIN_NAME = "admin";
	private static final String DEFAULT_ADMIN_PW = "admin";

	private String testUserToken;

	@Test
	public void init()
	{
		LOG.info("Read enviroment variables");
		String envHost = System.getenv("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getenv("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getenv("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		String adminToken = login(adminUserName, adminPassword);

		createTestUser(adminToken);

		String testUserId = getUserId(adminToken, "test");
		LOG.info("testUSerId: " + testUserId);


		grantRights(adminToken, testUserId, "sys_FreemarkerTemplate");
		grantRights(adminToken, testUserId, "sys_scr_ScriptType");

		this.testUserToken = login("test", "test");
	}

	/**
	 * Login with user name and password and return token on success
	 * @param userName the username to login with
	 * @param password the password to use for login
	 * @return the token returned from the login
	 */
	private String login(String userName, String password)
	{
		JSONObject loginBody = new JSONObject();
		loginBody.put("username", userName);
		loginBody.put("password", password);

		String token = given().log().all()
				.contentType(APPLICATION_JSON).body(loginBody.toJSONString())
				.when().post(PATH + "login")
				.then().log().all()
				.extract().path("token");

		LOG.info("Login token for user(" + userName + "): " + token);

		return token;
	}

	private void createTestUser(String adminToken)
	{
		JSONObject createTestUserBody = new JSONObject();
		createTestUserBody.put("active", true);
		createTestUserBody.put("username", "test");
		createTestUserBody.put("password_", "test");
		createTestUserBody.put("superuser", false);
		createTestUserBody.put("changePassword", false);
		createTestUserBody.put("Email", "test@example.com");

		int code = given().log().all()
				.header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(createTestUserBody.toJSONString())
				.when().post(PATH + "sys_sec_User")
				.then().log().all()
				.extract().statusCode();

		LOG.info("Created test user code: " + Integer.toString(code));
	}

	/**
	 * Grant user rights in list of entities
	 * @param adminToken the token to use for signin
	 * @param userId the ID (not the name) of the user that needs to get the rights
	 * @param entity a list of entity names
	 * @return
	 */
	private int grantRights(String adminToken, String userId, String entity)
	{
		String right =  "ROLE_ENTITY_WRITE_" + entity.toUpperCase();
		JSONObject body = new JSONObject(ImmutableMap.of("role", right, "User", userId));

		return given().log().all()
				.header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(body.toJSONString())
				.when().post(PATH + "sys_sec_UserAuthority")
				.then().log().all()
				.extract().statusCode();
	}

	private String getUserId(String adminToken, String userName)
	{

		Map<String, Object> query = of("q",
				singletonList(of("field", "username", "operator", "EQUALS", "value", userName)));
		JSONObject body = new JSONObject(query);

		String id = given()
				.header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.queryParam("_method", "GET")
				.body(body.toJSONString())
				.when().post(PATH + "sys_sec_User")
				.then().log().all()
				.extract().path("items[0].id");

		return id;

	}

	@Test
	public void testFreemarkerNotAllowed()
	{
		noPermissionRequest("sys_FreemarkerTemplate");
	}

	@Test
	public void testScriptTypeNotAllowed()
	{
		noPermissionRequest("sys_scr_ScriptType");
	}

	@Test
	public void testUserAuthorityotAllowed()
	{
		noPermissionRequest("sys_sec_UserAuthority");
	}

	@Test
	public void testGroupAuthorityNotAllowed()
	{
		noPermissionRequest("sys_sec_GroupAuthority");
	}

	@Test
	public void testGetFreemarkerSuccess()
	{
		successRequest("sys_FreemarkerTemplate", this.testUserToken);
	}

	@Test
	public void testGetScriptTypeSuccess()
	{
		successRequest("sys_scr_ScriptType", this.testUserToken);
	}

	private void noPermissionRequest(String requestedEntity)
	{
		given().log().all()
				.when().get(PATH + requestedEntity)
				.then().statusCode(401).body("errors.message[0]", equalTo("No [COUNT] permission on entity [" + requestedEntity + "]"));
	}


	private void successRequest(String requestedEntity, String token)
	{
		given().log().all()
				.header(X_MOLGENIS_TOKEN, token).contentType(APPLICATION_JSON)
				.when().get(PATH + requestedEntity)
				.then().log().all()
				.statusCode(200);
	}

}
