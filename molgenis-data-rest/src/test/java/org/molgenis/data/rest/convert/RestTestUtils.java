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
}
