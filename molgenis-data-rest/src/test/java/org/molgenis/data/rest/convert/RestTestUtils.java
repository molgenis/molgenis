package org.molgenis.data.rest.convert;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Methods shared by Rest Api tests
 */
public class RestTestUtils
{
	private static final Logger LOG = getLogger(RestTestUtils.class);

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

	public enum Permission
	{
		READ, WRITE, COUNT, NONE, WRITEMETA
	}

	/**
	 * Login with user name and password and return token on success.
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

		return given().contentType(APPLICATION_JSON)
					  .body(loginBody.toJSONString())
					  .when()
					  .post("api/v1/login")
					  .then()
					  .extract()
					  .path("token");
	}

	/**
	 * Create a user with userName and password as admin using given token.
	 *
	 * @param adminToken the token to use for login
	 * @param userName   the name of the user to create
	 * @param password   the password of the user to create
	 */
	public static void createUser(String adminToken, String userName, String password)
	{
		JSONObject createTestUserBody = new JSONObject();
		createTestUserBody.put("active", true);
		createTestUserBody.put("username", userName);
		createTestUserBody.put("password_", password);
		createTestUserBody.put("superuser", false);
		createTestUserBody.put("changePassword", false);
		createTestUserBody.put("Email", userName + "@example.com");

		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(createTestUserBody.toJSONString())
			   .when()
			   .post("api/v1/sys_sec_User");
	}

	/**
	 * Import emx file using add/update.
	 * <p>
	 * Importing is done async in the backend, but this methods waits for importing to be done.
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
			LOG.error(e.getMessage());
		}

		String importJobURL = given().multiPart(file)
									 .param("file")
									 .param("action", "ADD_UPDATE_EXISTING")
									 .header(X_MOLGENIS_TOKEN, adminToken)
									 .post("plugin/importwizard/importFile")
									 .then()
									 .extract()
									 .asString();

		// Remove the leading '/' character and leading and trailing '"' characters
		importJobURL = importJobURL.substring(2, importJobURL.length() - 1);
		LOG.info("############ " + importJobURL);

		// As importing is done async in the backend we poll the success url to check if importing is done.
		String importStatus = "RUNNING";
		while (importStatus.equals("RUNNING"))
		{
			importStatus = given().contentType(APPLICATION_JSON)
								  .header(X_MOLGENIS_TOKEN, adminToken)
								  .get(importJobURL)
								  .then()
								  .extract()
								  .path("status")
								  .toString();
			try
			{
				sleep(500L);
			}
			catch (InterruptedException e)
			{
				LOG.error(e.getMessage());
			}
			LOG.info("Status: " + importStatus);
		}
		LOG.info("Import completed");
	}

	/**
	 * Reads the contents of a file and stores it in a String.
	 * Useful if you want to compare the contents of a file with the response of an API endpoint,
	 * if that endpoint returns a file (e.g. /api/v1/csv/{entityName}.
	 *
	 * @param fileName the name of the file
	 * @return a string containing the files contents
	 */
	public static String getFileContents(String fileName)
	{
		URL resourceUrl = Resources.getResource(RestTestUtils.class, fileName);
		File file;
		try
		{
			file = new File(new URI(resourceUrl.toString()).getPath());
			return readFileToString(file);
		}
		catch (Exception e)
		{
			getLogger(RestTestUtils.class).error(e.getMessage());
		}
		return "";
	}

	/**
	 * Read json from file and return as Json object.
	 *
	 * @param fileName the file to read from the resources folder
	 * @return The json form the file a JSONObject
	 */
	public static JSONObject readJsonFile(String fileName)
	{
		JSONObject jsonObject = null;
		try
		{
			jsonObject = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(
					new FileReader(Resources.getResource(RestTestUtils.class, fileName).getFile()));

		}
		catch (ParseException | FileNotFoundException e)
		{
			LOG.error("Unable to readJsonFile(" + fileName + ")");
			LOG.error(e.getMessage());
		}

		return jsonObject;
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
	 * Get the id for a given entity.
	 *
	 * @param adminToken token for signin
	 * @param attribute  the field to filter on
	 * @param value      the value the filter should match
	 * @param entityName the entity name
	 * @return the id of the given entity
	 */
	public static String getEntityTypeId(String adminToken, String attribute, String value, String entityName)
	{
		Map<String, Object> query = of("q",
				singletonList(of("field", attribute, "operator", "EQUALS", "value", value)));
		JSONObject body = new JSONObject(query);

		return given().header("x-molgenis-token", adminToken)
					  .contentType(APPLICATION_JSON)
					  .queryParam("_method", "GET")
					  .body(body.toJSONString())
					  .when()
					  .post("api/v1/" + entityName)
					  .then()
					  .extract()
					  .path("items[0].id");
	}

	/**
	 * Grant user rights on non-system entity.
	 *
	 * @param adminToken the token to use for signin
	 * @param userId     the ID (not the name) of the user that needs to get the rights
	 * @param entity     a list of entity names
	 */
	public static void grantRights(String adminToken, String userId, String entity, Permission permission)
	{
		String entityTypeId = getEntityTypeId(adminToken, "id", entity, "sys_md_EntityType");
		grantSystemRights(adminToken, userId, entityTypeId, permission);
	}

	/**
	 * Grant user rights on system entity.
	 *
	 * @param adminToken the token to use for signin
	 * @param userId     the id of the user to give the permissions to
	 * @param entity     the entity on which the permissions are given
	 * @param permission the type of permission to give
	 */
	public static void grantSystemRights(String adminToken, String userId, String entity, Permission permission)
	{
		String right = "ROLE_ENTITY_" + permission + "_" + entity;
		JSONObject body = new JSONObject(ImmutableMap.of("role", right, "User", userId));

		given().header("x-molgenis-token", adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(body.toJSONString())
			   .when()
			   .post("api/v1/" + "sys_sec_UserAuthority");
	}

	/**
	 * @param adminToken   the token to use to authenticate
	 * @param permissionId the id of the permission to use
	 */
	public static void removeRight(String adminToken, String permissionId)
	{
		given().header("x-molgenis-token", adminToken)
			   .contentType(APPLICATION_JSON)
			   .delete("api/v1/sys_sec_UserAuthority/" + permissionId);
	}

	public static void removeEntity(String adminToken, String entityId)
	{
		given().header("x-molgenis-token", adminToken)
			   .contentType(APPLICATION_JSON)
			   .delete("api/v1/" + entityId + "/meta");
	}

	/**
	 * Removes permissions from UserAuthority table for a given user identifier
	 *
	 * @param adminToken
	 * @param testUserId
	 */
	public static void removeRightsForUser(String adminToken, String testUserId)
	{
		// get identifiers for permissions this user owns
		Map<String, Object> query = of("q",
				singletonList(of("field", "User", "operator", "EQUALS", "value", testUserId)));
		JSONObject body = new JSONObject(query);

		List<Map> permissions = given().header("x-molgenis-token", adminToken)
									   .contentType(APPLICATION_JSON)
									   .queryParam("_method", "GET")
									   .body(body.toJSONString())
									   .when()
									   .post("api/v1/" + "sys_sec_UserAuthority")
									   .then()
									   .extract()
									   .path("items");

		List<String> identifiers = permissions.stream()
											  .map(jsonObject -> jsonObject.get("id").toString())
											  .collect(Collectors.toList());

		// use identifiers to batch delete from User Authority table
		Map<String, List<String>> requestBody = newHashMap();
		requestBody.put("entityIds", identifiers);
		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(requestBody)
			   .delete("api/v2/sys_sec_UserAuthority");
	}

	/**
	 * Enable or disable 2 factor authentication
	 *
	 * @param adminToken admin token for login in RESTAPI
	 * @param state      state of 2 factor authentication (can be Enforced, Enabled, Disabled)
	 */
	public static void toggle2fa(String adminToken, TwoFactorAuthenticationSetting state)
	{

		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(state.toString())
			   .when()
			   .put("api/v1/sys_set_app/app/sign_in_2fa");
	}

}
