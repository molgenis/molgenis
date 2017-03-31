package org.molgenis.data.rest.convert;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import net.minidev.json.JSONObject;
import org.molgenis.data.rest.RestControllerIT;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;

/**
 * Methods shared by Rest Api tests
 */
public class RestTestUtils
{
	public static final String APPLICATION_JSON = "application/json";
	public static final String X_MOLGENIS_TOKEN = "x-molgenis-token";

	// Admin credentials
	public static final String DEFAULT_HOST = "http://localhost:8080";
	public static final String DEFAULT_ADMIN_NAME = "admin";
	public static final String DEFAULT_ADMIN_PW = "admin";

	public static final int OKE = 200;
	public static final int CREATED = 201;
	public static final int NO_CONTENT = 204;
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 401;
	public static final int NOT_FOUND = 404;

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

		return given()
				.log().all()
				.contentType(APPLICATION_JSON).body(loginBody.toJSONString()).when().post("api/v1/login").then()
				.log().all()
				.extract().path("token");
	}

	/**
	 * Create a user with testuserName and testUserPassword as admin using given token
	 *
	 * @param adminToken       the token to use for login
	 * @param testuserName     the name of the user to create
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

		given()
				//.log().all()
				.header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(createTestUserBody.toJSONString()).when().post("api/v1/sys_sec_User").then()
		//.log().all()
		;
	}

	/**
	 * Import emx file
	 * using add/update
	 *
	 * @param adminToken to use for login
	 * @param fileName   the file to upload
	 */
	public static void uploadEMX(String adminToken, String fileName)
	{
		URL resourceUrl = Resources.getResource(RestTestUtils.class, fileName);
		File file = null;
		try
		{
			file = new File(new URI(resourceUrl.toString()).getPath());
		}
		catch (URISyntaxException e)
		{
			LoggerFactory.getLogger(RestTestUtils.class).error(e.getMessage());
		}

		given().multiPart(file).param("file").param("action", "ADD_UPDATE_EXISTING")
				.header(X_MOLGENIS_TOKEN, adminToken).post("plugin/importwizard/importFile").then().log().all();
	}

	/**
	 * Get the used id by querying the user entity
	 *
	 * @param adminToken token for signin
	 * @param userName   the name of the user to fetch the id for
	 * @return the id of the user
	 */
	public static String getUserId(String adminToken, String userName)
	{
		return getEntityTypeId(adminToken, "username", userName, "sys_sec_User");
	}

	/**
	 * Get the id for a given entity
	 *
	 * @param adminToken token for signin
	 * @param attribute  the field to filter on
	 * @param value      the value the filter should match
	 * @param entityName the entity name
	 * @return the id of the given entity
	 */
	public static String getEntityTypeId(String adminToken, String attribute, String value,
			String entityName)
	{
		Map<String, Object> query = of("q",
				singletonList(of("field", attribute, "operator", "EQUALS", "value", value)));
		JSONObject body = new JSONObject(query);

		return given().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON).queryParam("_method", "GET")
				.body(body.toJSONString()).when().post("api/v1/" + entityName).then()
				//.log().all()
				.extract().path("items[0].id");
	}

	/**
	 * Grant user rights on non-system entity
	 *
	 * @param adminToken the token to use for signin
	 * @param userId     the ID (not the name) of the user that needs to get the rights
	 * @param entity     a list of entity names
	 */
	public static void grantRights(String adminToken, String permissionID, String userId, String entity,
			RestControllerIT.Permission permission)
	{
		String entityTypeId = getEntityTypeId(adminToken, "name", entity, "sys_md_EntityType");
		grantSystemRights(adminToken, permissionID, userId, entityTypeId, permission);
	}

	/**
	 * Grant user rights on system entity
	 *
	 * @param adminToken   the token to use for signin
	 * @param permissionID the id of the permission
	 * @param userId       the id of the user to give the permissions to
	 * @param entity       the entity on which the permissions are given
	 * @param permission   the type of permission to give
	 */
	public static void grantSystemRights(String adminToken, String permissionID, String userId,
			String entity, RestControllerIT.Permission permission)
	{
		String right = "ROLE_ENTITY_" + permission + "_" + entity;
		JSONObject body = new JSONObject(ImmutableMap.of("id", permissionID, "role", right, "User", userId));

		given()
				.log().all()
				.header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON).body(body.toJSONString()).when()
				.post("api/v1/" + "sys_sec_UserAuthority").then().log();
	}

	/**
	 * @param adminToken   the token to use to authenticate
	 * @param permissionId the id of the permission to use
	 */
	public static void removeRight(String adminToken, String permissionId)
	{
		given().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.delete("api/v1/sys_sec_UserAuthority/" + permissionId);
	}

	public static void removeEntity(String adminToken, String entityName)
	{
		given().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.delete("api/v1/" + entityName + "/meta").then().log().all();
	}
}
