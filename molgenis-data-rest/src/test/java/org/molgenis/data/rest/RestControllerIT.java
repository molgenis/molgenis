package org.molgenis.data.rest;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import net.minidev.json.JSONObject;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;

public class RestControllerIT
{
	private static final Logger LOG = LoggerFactory.getLogger(RestControllerIT.class);

	private static final String X_MOLGENIS_TOKEN = "x-molgenis-token";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String APPLICATION_JSON = "application/json";
	private static final String TEXT_CSV = "text/csv";
	private static final String PATH = "api/v1/";

	private static final String DEFAULT_HOST = "https://molgenis62.gcc.rug.nl/";
	private static final String DEFAULT_ADMIN_NAME = "admin";
	private static final String DEFAULT_ADMIN_PW = "admin";

	private String testUserToken;

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
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
	 *
	 * @param userName the username to login with
	 * @param password the password to use for login
	 * @return the token returned from the login
	 */
	private String login(String userName, String password)
	{
		JSONObject loginBody = new JSONObject();
		loginBody.put("username", userName);
		loginBody.put("password", password);

		String token = given().log().all().contentType(APPLICATION_JSON).body(loginBody.toJSONString()).when()
				.post(PATH + "login").then().log().all().extract().path("token");

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

		int code = given().log().all().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(createTestUserBody.toJSONString()).when().post(PATH + "sys_sec_User").then().log().all().extract()
				.statusCode();

		LOG.info("Created test user code: " + Integer.toString(code));
	}

	/**
	 * Grant user rights in list of entities
	 *
	 * @param adminToken the token to use for signin
	 * @param userId     the ID (not the name) of the user that needs to get the rights
	 * @param entity     a list of entity names
	 * @return
	 */
	private int grantRights(String adminToken, String userId, String entity)
	{
		String right = "ROLE_ENTITY_WRITE_" + entity.toUpperCase();
		JSONObject body = new JSONObject(ImmutableMap.of("role", right, "User", userId));

		return given().log().all().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.body(body.toJSONString()).when().post(PATH + "sys_sec_UserAuthority").then().log().all().extract()
				.statusCode();
	}

	private String getUserId(String adminToken, String userName)
	{

		Map<String, Object> query = of("q",
				singletonList(of("field", "username", "operator", "EQUALS", "value", userName)));
		JSONObject body = new JSONObject(query);

		String id = given().header("x-molgenis-token", adminToken).contentType(APPLICATION_JSON)
				.queryParam("_method", "GET").body(body.toJSONString()).when().post(PATH + "sys_sec_User").then().log()
				.all().extract().path("items[0].id");

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
	public void testUserAuthorityNotAllowed()
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

	@Test
	public void testEntityExists()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_PLAIN).when()
				.get(PATH + "sys_scr_ScriptType/exist").then().log().all().statusCode(200).body(equalTo("true"));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_PLAIN).when()
				.get(PATH + "sys_NonExistingEntity/exist").then().log().all().statusCode(200).body(equalTo("false"));
	}

	@Test
	public void testGetEntityType()
	{
		String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType/meta\",\"hrefCollection\":\"/api/v1/sys_scr_ScriptType\",\"name\":\"sys_scr_ScriptType\",\"label\":\"Script type\",\"attributes\":{\"name\":{\"href\":\"/api/v1/sys_scr_ScriptType/meta/name\"}},\"labelAttribute\":\"name\",\"idAttribute\":\"name\",\"lookupAttributes\":[],\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":true}";
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/meta").then().log().all().statusCode(200).body(equalTo(responseBody));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/meta?_method=GET").then().log().all().statusCode(200)
				.body(equalTo(responseBody));
	}

	@Test
	public void testRetrieveEntityAttributeMeta()
	{
		String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType/meta/name\",\"fieldType\":\"STRING\",\"name\":\"name\",\"label\":\"name\",\"attributes\":[],\"enumOptions\":[],\"maxLength\":255,\"auto\":false,\"nillable\":false,\"readOnly\":true,\"labelAttribute\":true,\"unique\":true,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false}";
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/meta/name").then().log().all().statusCode(200)
				.body(equalTo(responseBody));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/meta/name?_method=GET").then().log().all().statusCode(200)
				.body(equalTo(responseBody));
	}

	@Test
	public void testRetrieveEntity()
	{
		String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType/R\",\"name\":\"R\"}";
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/R").then().log().all().statusCode(200).body(equalTo(responseBody));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType/R?_method=GET").then().log().all().statusCode(200)
				.body(equalTo(responseBody));
	}

	@Test
	public void testRetrieveEntityAttribute()
	{
		String responseBody = "{\"href\":\"/api/v1/sys_md_Package/sys/children\",\"start\":0,\"num\":100,\"total\":8,\"items\":[{\"href\":\"/api/v1/sys_md_Package/sys_idx\",\"id\":\"sys_idx\",\"name\":\"idx\",\"label\":\"Index\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_idx/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_idx/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_idx/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_idx/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_mail\",\"id\":\"sys_mail\",\"name\":\"mail\",\"label\":\"Mail\",\"description\":\"Mail properties\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_mail/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_mail/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_mail/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_mail/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_map\",\"id\":\"sys_map\",\"name\":\"map\",\"label\":\"Mapper\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_map/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_map/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_map/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_map/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_md\",\"id\":\"sys_md\",\"name\":\"md\",\"label\":\"Meta\",\"description\":\"Package containing all meta data entities\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_md/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_md/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_md/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_md/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_ont\",\"id\":\"sys_ont\",\"name\":\"ont\",\"label\":\"Ontology\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_ont/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_ont/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_ont/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_ont/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_scr\",\"id\":\"sys_scr\",\"name\":\"scr\",\"label\":\"Script\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_scr/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_scr/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_scr/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_scr/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_sec\",\"id\":\"sys_sec\",\"name\":\"sec\",\"label\":\"Security\",\"description\":\"Package containing security related entities\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_sec/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_sec/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_sec/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_sec/tags\"}},{\"href\":\"/api/v1/sys_md_Package/sys_set\",\"id\":\"sys_set\",\"name\":\"set\",\"label\":\"Settings\",\"description\":\"Application and plugin settings\",\"parent\":{\"href\":\"/api/v1/sys_md_Package/sys_set/parent\"},\"children\":{\"href\":\"/api/v1/sys_md_Package/sys_set/children\"},\"entityTypes\":{\"href\":\"/api/v1/sys_md_Package/sys_set/entityTypes\"},\"tags\":{\"href\":\"/api/v1/sys_md_Package/sys_set/tags\"}}]}";
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_md_Package/sys/children").then().log().all().statusCode(200)
				.body(equalTo(responseBody));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_md_Package/sys/children?_method=GET").then().log().all().statusCode(200)
				.body(equalTo(responseBody));
	}

	@Test
	public void testRetrieveEntityCollectionResponse()
	{
		String responseBody = "{\"href\":\"/api/v1/sys_scr_ScriptType\",\"meta\":{\"href\":\"/api/v1/sys_scr_ScriptType/meta\",\"hrefCollection\":\"/api/v1/sys_scr_ScriptType\",\"name\":\"sys_scr_ScriptType\",\"label\":\"Script type\",\"attributes\":{\"name\":{\"href\":\"/api/v1/sys_scr_ScriptType/meta/name\"}},\"labelAttribute\":\"name\",\"idAttribute\":\"name\",\"lookupAttributes\":[],\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":true},\"start\":0,\"num\":100,\"total\":4,\"items\":[{\"href\":\"/api/v1/sys_scr_ScriptType/python\",\"name\":\"python\"},{\"href\":\"/api/v1/sys_scr_ScriptType/R\",\"name\":\"R\"},{\"href\":\"/api/v1/sys_scr_ScriptType/JavaScript%20(Magma)\",\"name\":\"JavaScript (Magma)\"},{\"href\":\"/api/v1/sys_scr_ScriptType/JavaScript\",\"name\":\"JavaScript\"}]}";
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType").then().log().all().statusCode(200).body(equalTo(responseBody));

		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(APPLICATION_JSON).when()
				.get(PATH + "sys_scr_ScriptType?_method=GET").then().log().all().statusCode(200)
				.body(equalTo(responseBody));
	}

	@Test
	public void testRetrieveEntityCollection()
	{
		given().log().all().header(X_MOLGENIS_TOKEN, this.testUserToken).contentType(TEXT_CSV).when()
				.get(PATH + "csv/sys_scr_ScriptType").then().log().all().statusCode(200).body(equalTo(
				"\"name\"\n" + "\"python\"\n" + "\"R\"\n" + "\"JavaScript (Magma)\"\n" + "\"JavaScript\"\n"));
	}

	private void noPermissionRequest(String requestedEntity)
	{
		given().log().all().when().get(PATH + requestedEntity).then().statusCode(401)
				.body("errors.message[0]", equalTo("No [COUNT] permission on entity [" + requestedEntity + "]"));
	}

	private void successRequest(String requestedEntity, String token)
	{
		given().log().all().header(X_MOLGENIS_TOKEN, token).contentType(APPLICATION_JSON).when()
				.get(PATH + requestedEntity).then().log().all().statusCode(200);
	}

}
