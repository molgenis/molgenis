package org.molgenis.api.tests.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.molgenis.data.security.auth.GroupMemberMetaData;
import org.molgenis.data.security.auth.GroupMetaData;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.molgenis.data.security.auth.GroupMemberMetaData.GROUP_MEMBER;
import static org.molgenis.data.security.auth.GroupMemberMetaData.USER;
import static org.molgenis.data.security.auth.GroupMetaData.NAME;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
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
		READ, WRITE, COUNT, WRITEMETA
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
		if (adminToken != null)
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

			addToAllUsersGroup(adminToken, userName);
		}
	}

	private static void addToAllUsersGroup(String adminToken, String userName)
	{

		JSONObject groupMembership = new JSONObject();
		groupMembership.put(USER, getUserId(adminToken, userName));
		groupMembership.put(GroupMemberMetaData.GROUP,
				getEntityTypeId(adminToken, NAME, ALL_USER_GROUP, GroupMetaData.GROUP));

		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(groupMembership.toJSONString())
			   .when()
			   .post("api/v1/" + GROUP_MEMBER);
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
		File file = getResourceFile(fileName);

		uploadEMXFile(adminToken, file);
	}

	/**
	 * Import emx file using add/update.
	 * <p>
	 * Importing is done async in the backend, but this methods waits for importing to be done.
	 *
	 * @param adminToken to use for login
	 * @param pathToFileFolder
	 * @param fileName name of the file to upload
	 */
	public static void uploadEMX(String adminToken, String pathToFileFolder, String fileName)
	{
		File file = new File(pathToFileFolder + File.separator + fileName);

		uploadEMXFile(adminToken, file);
	}

	private static void uploadEMXFile(String adminToken, File file)
	{
		String importJobURLString = given().multiPart(file)
										   .param("file")
										   .param("action", "ADD_UPDATE_EXISTING")
										   .header(X_MOLGENIS_TOKEN, adminToken)
										   .post("plugin/importwizard/importFile")
										   .then()
										   .extract()
										   .asString();

		// Remove the leading '/' character and leading and trailing '"' characters
		String importJobURL = importJobURLString.substring(2, importJobURLString.length() - 1);

		LOG.info("############ " + importJobURL);
		await().pollDelay(500, MILLISECONDS)
			   .atMost(5, MINUTES)
			   .until(() -> pollForStatus(adminToken, importJobURL), not(equalTo("RUNNING")));
		LOG.info("Import completed");
	}

	private static File getResourceFile(String fileName)
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
		return file;
	}

	/**
	 * Import emx file using add/update.
	 * <p>
	 * Importing is done async in the backend, but this methods waits for importing to be done.
	 *
	 * @param adminToken to use for login
	 * @param fileName   the file to upload
	 */
	public static void uploadVCF(String adminToken, String fileName, String entityName)
	{
		File file = getResourceFile(fileName);

		String importJobURLString = given().multiPart(file)
										   .param("file")
										   .param("action", "ADD")
										   .param("entityName", entityName)
										   .header(X_MOLGENIS_TOKEN, adminToken)
										   .post("plugin/importwizard/importFile")
										   .then()
										   .extract()
										   .asString();

		// Remove the leading '/' character and leading and trailing '"' characters
		String importJobURL = importJobURLString.substring(2, importJobURLString.length() - 1);

		LOG.info("############ " + importJobURL);
		await().pollDelay(500, MILLISECONDS)
			   .atMost(5, MINUTES)
			   .until(() -> pollForStatus(adminToken, importJobURL), not(equalTo("RUNNING")));
		LOG.info("Import completed");
	}

	private static String pollForStatus(String adminToken, String importJobURL)
	{
		return given().contentType(APPLICATION_JSON)
					  .header(X_MOLGENIS_TOKEN, adminToken)
					  .get(importJobURL)
					  .then()
					  .extract()
					  .path("status")
					  .toString();
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
					new InputStreamReader(RestTestUtils.class.getResourceAsStream(fileName), Charset.forName("UTF-8")));

		}
		catch (ParseException e)
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
	private static String getEntityTypeId(String adminToken, String attribute, String value, String entityName)
	{
		Map<String, Object> query = of("q",
				singletonList(of("field", attribute, "operator", "EQUALS", "value", value)));
		JSONObject body = new JSONObject(query);

		return given().header(X_MOLGENIS_TOKEN, adminToken)
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
	 * Grant user read right on given plugin
	 *
	 * @param adminToken the token to use for signin
	 * @param userId     the ID (not the name) of the user that needs to get the rights
	 * @param plugin     the name of the plugin
	 */
	public static void grantPluginRights(String adminToken, String userId, String plugin)
	{
		String right = "ROLE_PLUGIN_READ_" + plugin;
		JSONObject body = new JSONObject(ImmutableMap.of("role", right, "User", userId));

		given().header(X_MOLGENIS_TOKEN, adminToken)
			   .contentType(APPLICATION_JSON)
			   .body(body.toJSONString())
			   .when()
			   .post("api/v1/" + "sys_sec_UserAuthority");
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
		if (adminToken != null)
		{
			String right = "ROLE_ENTITY_" + permission + "_" + entity;
			JSONObject body = new JSONObject(ImmutableMap.of("role", right, "User", userId));

			given().header(X_MOLGENIS_TOKEN, adminToken)
				   .contentType(APPLICATION_JSON)
				   .body(body.toJSONString())
				   .when()
				   .post("api/v1/" + "sys_sec_UserAuthority");
		}
	}

	public static void removePackages(String adminToken, List<String> packageNames)
	{
		packageNames.forEach(packageName -> removePackage(adminToken, packageName));
	}

	private static void removePackage(String adminToken, String packageName)
	{
		if (adminToken != null && packageName != null)
		{
			given().header(X_MOLGENIS_TOKEN, adminToken)
				   .contentType(APPLICATION_JSON)
				   .delete("api/v1/sys_md_Package/" + packageName);
		}
	}

	public static void removeEntities(String adminToken, List<String> entities)
	{
		entities.forEach(entity -> removeEntity(adminToken, entity));
	}

	public static void removeEntity(String adminToken, String entityId)
	{
		if (adminToken != null && entityId != null)
		{
			given().header(X_MOLGENIS_TOKEN, adminToken)
				   .contentType(APPLICATION_JSON)
				   .delete("api/v1/" + entityId + "/meta");
		}
	}

	public static void removeEntityFromTable(String adminToken, String entityTypeId, String entityId)
	{
		if (adminToken != null && entityTypeId != null && entityId != null)
		{
			given().header(X_MOLGENIS_TOKEN, adminToken)
				   .contentType(APPLICATION_JSON)
				   .delete("api/v1/" + entityTypeId + "/" + entityId);
		}
	}

	public static void removeImportJobs(String adminToken, List<String> jobIds)
	{
		jobIds.forEach(jobId -> removeImportJob(adminToken, jobId));
	}

	private static void removeImportJob(String adminToken, String jobId)
	{
		if (adminToken != null && jobId != null)
		{
			given().header(X_MOLGENIS_TOKEN, adminToken)
				   .contentType(APPLICATION_JSON)
				   .delete("api/v2/sys_job_OneClickImportJobExecution/" + jobId);
		}
	}

	/**
	 * Removes permissions from UserAuthority table for a given user identifier
	 */
	public static void removeRightsForUser(String adminToken, String testUserId)
	{
		if (adminToken != null && testUserId != null)
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
	}

	/**
	 * Removes the token for the test user by logging out
	 */
	public static void cleanupUserToken(String testUserToken)
	{
		if (testUserToken != null)
		{
			given().header(X_MOLGENIS_TOKEN, testUserToken).when().post("api/v1/logout");
		}
	}

	/**
	 * Remove the test user by deleting the row from the User table
	 */
	public static void cleanupUser(String adminToken, String testUserId)
	{
		if (adminToken != null && testUserId != null)
		{
			given().header(X_MOLGENIS_TOKEN, adminToken).when().delete("api/v1/sys_sec_User/" + testUserId);
		}
	}
}
