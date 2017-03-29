package org.molgenis.data.rest.convert;

import net.minidev.json.JSONObject;

import static io.restassured.RestAssured.given;

/**
 * Methods shared by Rest Api tests
 */
public class RestTestUtils
{
	public static final String APPLICATION_JSON = "application/json";

	/**
	 * Login with user name and password and return token on success
	 *
	 * @param userName the username to login with
	 * @param password the password to use for login
	 * @return the token returned from the login
	 */
	public static String login(String userName, String password)
	{
		JSONObject loginBody = new JSONObject();
		loginBody.put("username", userName);
		loginBody.put("password", password);

		return given().log().all().contentType(APPLICATION_JSON).body(loginBody.toJSONString()).when()
				.post("api/v1/login").then().log().all().extract().path("token");
	}

	/**
	 * Create a user with testuserName and testUserPassword as admin using given token
	 * @param adminToken the token to use for login
	 * @param testuserName the name of the user to create
	 * @param testUserPassword the password of the user to create
	 */
	public static void createUser(String adminToken, String testuserName, String testUserPassword)
	{
		JSONObject createTestUserBody = new JSONObject();
		createTestUserBody.put("active", true);
		createTestUserBody.put("username", testuserName);
		createTestUserBody.put("password_", testUserPassword);
		createTestUserBody.put("superuser", false);
		createTestUserBody.put("changePassword", false);
		createTestUserBody.put("Email", testuserName + "@example.com");

		given().log().all().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(createTestUserBody.toJSONString()).when().post("api/v1/sys_sec_User").then().log().all();
	}
}
