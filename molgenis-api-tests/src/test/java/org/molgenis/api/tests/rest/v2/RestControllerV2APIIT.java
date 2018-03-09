package org.molgenis.api.tests.rest.v2;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static io.restassured.RestAssured.given;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.collections.Maps.newHashMap;

/**
 * Tests each endpoint of the V2 Rest Api through http calls
 */
public class RestControllerV2APIIT
{
	private static final Logger LOG = getLogger(RestControllerV2APIIT.class);

	private static final String REST_TEST_USER_PASSWORD = "api_v2_test_user_password";
	private static final String V2_TEST_FILE = "/RestControllerV2_API_TestEMX.xlsx";
	private static final String V2_DELETE_TEST_FILE = "/RestControllerV2_DeleteEMX.xlsx";
	private static final String V2_COPY_TEST_FILE = "/RestControllerV2_CopyEMX.xlsx";
	private static final String API_V2 = "api/v2/";

	private String testUserName;
	private String testUserToken;
	private String adminToken;
	private String testUserId;

	// Fields to store created entity ids from import test, used during cleanup to remove the entities
	private List<String> importedEntities = new ArrayList<>();
	private List<String> importPackages = new ArrayList<>();
	private List<String> importJobIds = new ArrayList<>();

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

		LOG.info("Importing Test data");
		uploadEMX(adminToken, V2_TEST_FILE);
		uploadEMX(adminToken, V2_DELETE_TEST_FILE);
		uploadEMX(adminToken, V2_COPY_TEST_FILE);
		LOG.info("Importing Done");

		testUserName = "api_v2_test_user" + System.currentTimeMillis();
		createUser(adminToken, testUserName, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, testUserName);

		setGrantedRepositoryPermissions(adminToken, testUserId,
				ImmutableMap.<String, Permission>builder().put("sys_md_Package", WRITE)
														  .put("sys_md_EntityType", WRITE)
														  .put("sys_md_Attribute", WRITE)
														  .put("sys_FileMeta", WRITE)
														  .put("sys_L10nString", WRITE)
														  .put("V2_API_TypeTestAPIV2", WRITE)
														  .put("V2_API_TypeTestRefAPIV2", WRITE)
														  .put("V2_API_LocationAPIV2", WRITE)
														  .put("V2_API_PersonAPIV2", WRITE)
														  .put("base_v2APITest1", WRITEMETA)
														  .put("base_v2APITest2", WRITEMETA)
														  .put("base_APICopyTest", WRITEMETA)
														  .put("sys_job_JobExecution", READ)
														  .put("sys_job_OneClickImportJobExecution", READ)
														  .build());

		testUserToken = login(testUserName, REST_TEST_USER_PASSWORD);
	}

	@Test(enabled = false) // TODO
	public void testGetVersion()
	{
		//	@Autowired
		//	@GetMapping("/version")
		//	@ResponseBody
		//	public Map<String, String> getVersion(@Value("${molgenis.version:@null}") String molgenisVersion,
		//			@Value("${molgenis.build.date:@null}") String molgenisBuildDate)
	}

	@Test
	public void testRetrieveEntity()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .get(API_V2 + "V2_API_TypeTestRefAPIV2/ref1")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityWithoutAttributeFilter(response);
	}

	@Test
	public void testRetrieveEntityPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .post(API_V2 + "V2_API_TypeTestRefAPIV2/ref1?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityWithoutAttributeFilter(response);
	}

	@Test
	public void testRetrieveEntityWithAttributeFilter()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .param("attrs", newArrayList("label"))
											  .get(API_V2 + "V2_API_TypeTestRefAPIV2/ref1")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityWithAttributeFilter(response);
	}

	@Test
	public void testRetrieveEntityIncludingCategories()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .param("includeCategories", newArrayList("true"))
											  .param("attrs", newArrayList("xcategorical_value"))
											  .get(API_V2 + "V2_API_TypeTestAPIV2/1")
											  .then()
											  .log()
											  .all();
		response.statusCode(OKE);
		response.body("_meta.attributes[0].categoricalOptions.id", Matchers.hasItems("ref1", "ref2","ref3"));
	}

	@Test
	public void testRetrieveEntityExcludingCategoriesResultsInNoCategoricalOptions()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .param("includeCategories", newArrayList("false"))
											  .param("attrs", newArrayList("xcategorical_value"))
											  .get(API_V2 + "V2_API_TypeTestAPIV2/1")
											  .then()
											  .log()
											  .all();
		response.statusCode(OKE);
		response.body("_meta.attributes[0].categoricalOptions", Matchers.nullValue());
	}

	@Test
	public void testRetrieveEntityWithoutSettingCategoriesResultsInNoCategoricalOptions()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .param("attrs", newArrayList("xcategorical_value"))
											  .get(API_V2 + "V2_API_TypeTestAPIV2/1")
											  .then()
											  .log()
											  .all();
		response.statusCode(OKE);
		response.body("_meta.attributes[0].categoricalOptions", Matchers.nullValue());
	}

	@Test
	public void testRetrieveEntityWithAttributeFilterPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .param("attrs", newArrayList("label"))
											  .post(API_V2 + "V2_API_TypeTestRefAPIV2/ref1?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityWithAttributeFilter(response);
	}

	@Test
	public void testDeleteEntity()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .delete(API_V2 + "base_v2APITest1/ref1")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "base_v2APITest1")
			   .then()
			   .log()
			   .all()
			   .body("total", Matchers.equalTo(4), "items[0].value", Matchers.equalTo("ref2"), "items[1].value",
					   Matchers.equalTo("ref3"), "items[2].value", Matchers.equalTo("ref4"), "items[3].value",
					   Matchers.equalTo("ref5"));
	}

	@Test
	public void testDeleteEntityCollection()
	{
		Map<String, List<String>> requestBody = newHashMap();
		requestBody.put("entityIds", newArrayList("ref1", "ref2", "ref3", "ref4"));
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType(APPLICATION_JSON)
			   .body(requestBody)
			   .delete(API_V2 + "base_v2APITest2")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "base_v2APITest2")
			   .then()
			   .log()
			   .all()
			   .body("total", Matchers.equalTo(1), "items[0].value", Matchers.equalTo("ref5"));
	}

	@Test
	public void testRetrieveEntityCollection()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .get(API_V2 + "V2_API_TypeTestRefAPIV2")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityCollection(response);
	}

	@Test
	public void testRetrieveEntityCollectionPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .post(API_V2 + "V2_API_TypeTestRefAPIV2?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityCollection(response);
	}

	@Test
	public void testRetrieveEntityAttributeMeta()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .get(API_V2 + "V2_API_TypeTestRefAPIV2/meta/value")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test
	public void testRetrieveEntityAttributeMetaPost()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .post(API_V2 + "V2_API_TypeTestRefAPIV2/meta/value?_method=GET")
											  .then()
											  .log()
											  .all();
		validateRetrieveEntityAttributeMeta(response);
	}

	@Test(enabled = false) // FIXME
	public void testCreateEntities()
	{
		//		@Transactional
		//		@PostMapping(value = "/{entityName}", produces = APPLICATION_JSON_VALUE)
		//		@ResponseBody
		//		public EntityCollectionBatchCreateResponseBodyV2 createEntities(@PathVariable("entityName") String entityName,
		//			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

		JSONObject jsonObject = new JSONObject();
		JSONArray entities = new JSONArray();

		JSONObject entity1 = new JSONObject();
		entity1.put("value", "ref55");
		entity1.put("label", "label55");
		entities.add(entity1);

		JSONObject entity2 = new JSONObject();
		entity2.put("value", "ref57");
		entity2.put("label", "label57");
		entities.add(entity2);

		jsonObject.put("entities", entities);

		given().log()
			   .all()
			   .body(jsonObject.toJSONString())
			   .contentType(APPLICATION_JSON)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V2 + "V2_API_TypeTestAPIV2")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED)
			   .body("location", Matchers.equalTo("/api/v2/V2_API_TypeTestv2?q=id=in=(\"55\",\"57\")"),
					   "resources[0].href", Matchers.equalTo("/api/v2/V2_API_TypeTestAPIV2/55"), "resources[1].href",
					   Matchers.equalTo("/api/v2/V2_API_TypeTestAPIV2/57"));
	}

	@Test
	public void testCopyEntity()
	{
		Map<String, String> request = newHashMap();
		request.put("newEntityName", "base_CopiedEntity");

		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(request)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .post(API_V2 + "copy/base_APICopyTest")
			   .then()
			   .log()
			   .all();

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "base_CopiedEntity")
			   .then()
			   .log()
			   .all()
			   .body("href", Matchers.equalTo("/api/v2/base_CopiedEntity"), "items[0].label",
					   Matchers.equalTo("Copied!"));
	}

	@Test(enabled = false) // FIXME
	public void testUpdateEntities()
	{
		//		@PutMapping(value = "/{entityName}")
		//		public synchronized void updateEntities(@PathVariable("entityName") String entityName,
		//			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

		Map<String, List<Object>> request = newHashMap();
		Map<String, Object> entity = newHashMap();
		entity.put("id", "ref1");
		entity.put("xstring", "This is an updated entity!");

		request.put("entities", newArrayList(entity));

		given().log()
			   .all()
			   .contentType(APPLICATION_JSON)
			   .body(request)
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .put(API_V2 + "V2_API_TypeTestRefAPIV2")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE);
	}

	@Test(enabled = false) // TODO
	public void testUpdateAttribute()
	{
		//	@PutMapping(value = "/{entityName}/{attributeName}")
		//	@ResponseStatus(OK)
		//	public synchronized void updateAttribute(@PathVariable("entityName") String entityName,
		//			@PathVariable("attributeName") String attributeName,
		//			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

		given().log().all().header(X_MOLGENIS_TOKEN, testUserToken).
				put(API_V2 + "base_v2APITest1/label").then().log().all().statusCode(OKE);
	}

	@Test
	public void testGetI18nStrings()
	{
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .get(API_V2 + "i18n")
											  .then()
											  .log()
											  .all();
		validateGetI18nStrings(response);
	}

	@Test
	public void testGetL10nStrings()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "i18n/form/en")
			   .then()
			   .log()
			   .all()
			   .statusCode(OKE)
			   .body("form_number_control_placeholder", Matchers.equalTo("Number"), "form_bool_true",
					   Matchers.equalTo("Yes"), "form_xref_control_placeholder", Matchers.equalTo("Search for a Value"),
					   "form_date_control_placeholder", Matchers.equalTo("Date"), "form_url_control_placeholder",
					   Matchers.equalTo("URL"), "form_computed_control_placeholder",
					   Matchers.equalTo("This value is computed automatically"), "form_email_control_placeholder",
					   Matchers.equalTo("Email"), "form_bool_false", Matchers.equalTo("No"), "form_bool_missing",
					   Matchers.equalTo("N/A"), "form_mref_control_placeholder", Matchers.equalTo("Search for Values"));
	}

	@Test
	public void testGetL10nProperties() throws IOException
	{
		String response = given().log()
								 .all()
								 .header(X_MOLGENIS_TOKEN, testUserToken)
								 .get(API_V2 + "i18n/form_en.properties")
								 .then()
								 .log()
								 .all()
								 .contentType("text/plain;charset=UTF-8")
								 .statusCode(OKE)
								 .extract()
								 .asString();

		Properties responseProperties = new Properties();
		responseProperties.load(new StringReader(response));

		InputStream is = getClass().getResourceAsStream("/testGetL10nProperties_response.properties");
		Properties expectedProperties = new Properties();
		expectedProperties.load(new InputStreamReader(is, "UTF-8"));

		assertEquals(responseProperties, expectedProperties);
	}

	@Test
	public void testRegisterMissingResourceStrings()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .contentType("application/x-www-form-urlencoded;charset=UTF-8")
			   .formParam("my_test_key", "test")
			   .post(API_V2 + "i18n/apiv2test")
			   .then()
			   .log()
			   .all()
			   .statusCode(CREATED);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "sys_L10nString?q=namespace==apiv2test")
			   .then()
			   .log()
			   .all()
			   .body("items[0].msgid", Matchers.equalTo("my_test_key"), "items[0].namespace",
					   Matchers.equalTo("apiv2test"));
	}

	@Test(dependsOnMethods = "testRegisterMissingResourceStrings")
	public void testDeleteNamespace()
	{
		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .delete(API_V2 + "i18n/apiv2test")
			   .then()
			   .log()
			   .all()
			   .statusCode(RestTestUtils.NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "sys_L10nString?q=namespace==apiv2test")
			   .then()
			   .log()
			   .all()
			   .body("total", Matchers.equalTo(0));
	}

	private void validateRetrieveEntityWithoutAttributeFilter(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("_meta.href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.hrefCollection",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.name",
				Matchers.equalTo("V2_API_TypeTestRefAPIV2"), "_meta.label", Matchers.equalTo("TypeTestRefAPIV2"),
				"_meta.description", Matchers.equalTo("MOLGENIS Data types test ref entity"),
				"_meta.attributes[0].href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"),
				"_meta.attributes[0].fieldType", Matchers.equalTo("STRING"), "_meta.attributes[0].name",
				Matchers.equalTo("value"), "_meta.attributes[0].label", Matchers.equalTo("value label"),
				"_meta.attributes[0].description", Matchers.equalTo("TypeTestRef value attribute"),
				"_meta.attributes[0].attributes", Matchers.equalTo(newArrayList()), "_meta.attributes[0].maxLength",
				Matchers.equalTo(255), "_meta.attributes[0].auto", Matchers.equalTo(false),
				"_meta.attributes[0].nillable", Matchers.equalTo(false), "_meta.attributes[0].readOnly",
				Matchers.equalTo(true), "_meta.attributes[0].labelAttribute", Matchers.equalTo(false),
				"_meta.attributes[0].unique", Matchers.equalTo(true), "_meta.attributes[0].visible",
				Matchers.equalTo(true), "_meta.attributes[0].lookupAttribute", Matchers.equalTo(true),
				"_meta.attributes[0].isAggregatable", Matchers.equalTo(false), "_meta.attributes[1].href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"), "_meta.attributes[1].fieldType",
				Matchers.equalTo("STRING"), "_meta.attributes[1].name", Matchers.equalTo("label"),
				"_meta.attributes[1].label", Matchers.equalTo("label label"), "_meta.attributes[1].description",
				Matchers.equalTo("TypeTestRef label attribute"), "_meta.attributes[1].attributes",
				Matchers.equalTo(newArrayList()), "_meta.attributes[1].maxLength", Matchers.equalTo(255),
				"_meta.attributes[1].auto", Matchers.equalTo(false), "_meta.attributes[1].nillable",
				Matchers.equalTo(false), "_meta.attributes[1].readOnly", Matchers.equalTo(false),
				"_meta.attributes[1].labelAttribute", Matchers.equalTo(true), "_meta.attributes[1].unique",
				Matchers.equalTo(false), "_meta.attributes[1].visible", Matchers.equalTo(true),
				"_meta.attributes[1].lookupAttribute", Matchers.equalTo(true), "_meta.attributes[1].isAggregatable",
				Matchers.equalTo(false), "_meta.labelAttribute", Matchers.equalTo("label"), "_meta.idAttribute",
				Matchers.equalTo("value"), "_meta.lookupAttributes", Matchers.equalTo(newArrayList("value", "label")),
				"_meta.isAbstract", Matchers.equalTo(false), "_meta.writable", Matchers.equalTo(true),
				"_meta.languageCode", Matchers.equalTo("en"), "_href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "value", Matchers.equalTo("ref1"), "label",
				Matchers.equalTo("label1"));
	}

	private void validateRetrieveEntityWithAttributeFilter(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("_meta.href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.hrefCollection",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.name",
				Matchers.equalTo("V2_API_TypeTestRefAPIV2"), "_meta.label", Matchers.equalTo("TypeTestRefAPIV2"),
				"_meta.description", Matchers.equalTo("MOLGENIS Data types test ref entity"),
				"_meta.attributes[0].href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"),
				"_meta.attributes[0].fieldType", Matchers.equalTo("STRING"), "_meta.attributes[0].name",
				Matchers.equalTo("label"), "_meta.attributes[0].label", Matchers.equalTo("label label"),
				"_meta.attributes[0].description", Matchers.equalTo("TypeTestRef label attribute"),
				"_meta.attributes[0].attributes", Matchers.equalTo(newArrayList()), "_meta.attributes[0].maxLength",
				Matchers.equalTo(255), "_meta.attributes[0].auto", Matchers.equalTo(false),
				"_meta.attributes[0].nillable", Matchers.equalTo(false), "_meta.attributes[0].readOnly",
				Matchers.equalTo(false), "_meta.attributes[0].labelAttribute", Matchers.equalTo(true),
				"_meta.attributes[0].unique", Matchers.equalTo(false), "_meta.attributes[0].visible",
				Matchers.equalTo(true), "_meta.attributes[0].lookupAttribute", Matchers.equalTo(true),
				"_meta.attributes[0].isAggregatable", Matchers.equalTo(false), "_meta.labelAttribute",
				Matchers.equalTo("label"), "_meta.idAttribute", Matchers.equalTo("value"), "_meta.lookupAttributes",
				Matchers.equalTo(newArrayList("value", "label")), "_meta.isAbstract", Matchers.equalTo(false),
				"_meta.writable", Matchers.equalTo(true), "_meta.languageCode", Matchers.equalTo("en"), "_href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "label", Matchers.equalTo("label1"));
	}

	private void validateRetrieveEntityCollection(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.hrefCollection",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.name",
				Matchers.equalTo("V2_API_TypeTestRefAPIV2"), "meta.label", Matchers.equalTo("TypeTestRefAPIV2"),
				"meta.description", Matchers.equalTo("MOLGENIS Data types test ref entity"), "meta.attributes[0].href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"), "meta.attributes[0].fieldType",
				Matchers.equalTo("STRING"), "meta.attributes[0].name", Matchers.equalTo("value"),
				"meta.attributes[0].label", Matchers.equalTo("value label"), "meta.attributes[0].description",
				Matchers.equalTo("TypeTestRef value attribute"), "meta.attributes[0].attributes",
				Matchers.equalTo(newArrayList()), "meta.attributes[0].maxLength", Matchers.equalTo(255),
				"meta.attributes[0].auto", Matchers.equalTo(false), "meta.attributes[0].nillable",
				Matchers.equalTo(false), "meta.attributes[0].readOnly", Matchers.equalTo(true),
				"meta.attributes[0].labelAttribute", Matchers.equalTo(false), "meta.attributes[0].unique",
				Matchers.equalTo(true), "meta.attributes[0].visible", Matchers.equalTo(true),
				"meta.attributes[0].lookupAttribute", Matchers.equalTo(true), "meta.attributes[0].isAggregatable",
				Matchers.equalTo(false), "meta.attributes[1].href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"), "meta.attributes[1].fieldType",
				Matchers.equalTo("STRING"), "meta.attributes[1].name", Matchers.equalTo("label"),
				"meta.attributes[1].label", Matchers.equalTo("label label"), "meta.attributes[1].description",
				Matchers.equalTo("TypeTestRef label attribute"), "meta.attributes[1].attributes",
				Matchers.equalTo(newArrayList()), "meta.attributes[1].maxLength", Matchers.equalTo(255),
				"meta.attributes[1].auto", Matchers.equalTo(false), "meta.attributes[1].nillable",
				Matchers.equalTo(false), "meta.attributes[1].readOnly", Matchers.equalTo(false),
				"meta.attributes[1].labelAttribute", Matchers.equalTo(true), "meta.attributes[1].unique",
				Matchers.equalTo(false), "meta.attributes[1].visible", Matchers.equalTo(true),
				"meta.attributes[1].lookupAttribute", Matchers.equalTo(true), "meta.labelAttribute",
				Matchers.equalTo("label"), "meta.idAttribute", Matchers.equalTo("value"), "meta.lookupAttributes",
				Matchers.equalTo(newArrayList("value", "label")), "meta.isAbstract", Matchers.equalTo(false),
				"meta.writable", Matchers.equalTo(true), "meta.languageCode", Matchers.equalTo("en"), "start",
				Matchers.equalTo(0), "num", Matchers.equalTo(100), "total", Matchers.equalTo(5), "items[0]._href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "items[0].value", Matchers.equalTo("ref1"),
				"items[0].label", Matchers.equalTo("label1"), "items[1]._href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref2"), "items[1].value", Matchers.equalTo("ref2"),
				"items[1].label", Matchers.equalTo("label2"), "items[2]._href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref3"), "items[2].value", Matchers.equalTo("ref3"),
				"items[2].label", Matchers.equalTo("label3"), "items[3]._href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref4"), "items[3].value", Matchers.equalTo("ref4"),
				"items[3].label", Matchers.equalTo("label4"), "items[4]._href",
				Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref5"), "items[4].value", Matchers.equalTo("ref5"),
				"items[4].label", Matchers.equalTo("label5"));
	}

	private void validateRetrieveEntityAttributeMeta(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("href", Matchers.equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"), "fieldType",
				Matchers.equalTo("STRING"), "name", Matchers.equalTo("value"), "label", Matchers.equalTo("value label"),
				"description", Matchers.equalTo("TypeTestRef value attribute"), "attributes",
				Matchers.equalTo(newArrayList()), "maxLength", Matchers.equalTo(255), "auto", Matchers.equalTo(false),
				"nillable", Matchers.equalTo(false), "readOnly", Matchers.equalTo(true), "labelAttribute",
				Matchers.equalTo(false), "unique", Matchers.equalTo(true), "visible", Matchers.equalTo(true),
				"lookupAttribute", Matchers.equalTo(true), "isAggregatable", Matchers.equalTo(false));
	}

	private void validateGetI18nStrings(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("dataexplorer_wizard_cancel", Matchers.equalTo("Cancel"),
				"questionnaires_table_view_questionnaire_button", Matchers.equalTo("View questionnaire"),
				"dataexplorer_aggregates_title", Matchers.equalTo("Aggregates"),
				"dataexplorer_directory_export_no_filters",
				Matchers.equalTo("Please filter the collections before sending a request to the negotiator."),
				"questionnaire_submit", Matchers.equalTo("Submit"), "questionnaires_title",
				Matchers.equalTo("My questionnaires"), "form_bool_false", Matchers.equalTo("No"), "form_bool_missing",
				Matchers.equalTo("N/A"), "form_mref_control_placeholder", Matchers.equalTo("Search for Values"),
				"dataexplorer_directory_export_dialog_message", Matchers.equalTo(
						"Your current selection of biobanks along with your filtering criteria will be sent to the BBMRI Negotiator. Are you sure?"),
				"questionnaires_table_status_header", Matchers.equalTo("Status"),
				"dataexplorer_directory_export_dialog_yes", Matchers.equalTo("Yes, Send to Negotiator"),
				"dataexplorer_wizard_button", Matchers.equalTo("Wizard"), "dataexplorer_wizard_apply",
				Matchers.equalTo("Apply"), "questionnaires_table_status_open", Matchers.equalTo("Open"),
				"form_bool_true", Matchers.equalTo("Yes"), "form_xref_control_placeholder",
				Matchers.equalTo("Search for a Value"), "form_url_control_placeholder", Matchers.equalTo("URL"),
				"questionnaires_table_continue_questionnaire_button", Matchers.equalTo("Continue questionnaire"),
				"questionnaires_table_questionnaire_header", Matchers.equalTo("Questionnaire"),
				"dataexplorer_aggregates_missing", Matchers.equalTo("N/A"), "questionnaires_description",
				Matchers.equalTo("Submitted and open questionnaires"), "questionnaires_table_status_submitted",
				Matchers.equalTo("Submitted"), "questionnaire_save_and_continue",
				Matchers.equalTo("Save and continue later"), "dataexplorer_directory_export_button",
				Matchers.equalTo("Go to sample / data negotiation"), "dataexplorer_data_data_item_filters",
				Matchers.equalTo("Data item filters"), "dataexplorer_aggregates_total", Matchers.equalTo("Total"),
				"dataexplorer_directory_export_dialog_title", Matchers.equalTo("Send request to the BBMRI Negotiator?"),
				"questionnaires_table_status_not_started", Matchers.equalTo("Not started yet"),
				"form_email_control_placeholder", Matchers.equalTo("Email"), "form_computed_control_placeholder",
				Matchers.equalTo("This value is computed automatically"), "dataexplorer_aggregates_no_result_message",
				Matchers.equalTo("No results found"), "questionnaires_table_start_questionnaire_button",
				Matchers.equalTo("Start questionnaire"), "dataexplorer_wizard_title", Matchers.equalTo("Filter Wizard"),
				"dataexplorer_aggregates_distinct", Matchers.equalTo("Distinct"), "form_number_control_placeholder",
				Matchers.equalTo("Number"), "dataexplorer_aggregates_group_by", Matchers.equalTo("Group by"),
				"dataexplorer_directory_export_dialog_no", Matchers.equalTo("No, I want to keep filtering"),
				"form_date_control_placeholder", Matchers.equalTo("Date"),
				"questionnaires_no_questionnaires_found_message", Matchers.equalTo("No questionnaires found"));
	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		// Delete imported file
		// TODO at the moment the Rest API has no way to remove/delete a file.

		// Delete import jobs
		removeImportJobs(adminToken, importJobIds);

		// Delete imported entity
		removeEntities(adminToken, importedEntities);

		// Delete created packages
		removePackages(adminToken, importPackages);

		// Clean up TestEMX
		removeEntity(adminToken, "V2_API_TypeTestAPIV2");
		removeEntity(adminToken, "V2_API_TypeTestRefAPIV2");
		removeEntity(adminToken, "V2_API_LocationAPIV2");
		removeEntity(adminToken, "V2_API_PersonAPIV2");

		removeEntity(adminToken, "base_v2APITest1");
		removeEntity(adminToken, "base_v2APITest2");

		removeEntity(adminToken, "base_APICopyTest");
		removeEntity(adminToken, "base_CopiedEntity");

		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		cleanupUserToken(testUserToken);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}

}