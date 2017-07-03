package org.molgenis.data.rest.v2;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.data.rest.convert.RestTestUtils.*;
import static org.molgenis.data.rest.convert.RestTestUtils.Permission.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertEquals;
import static org.testng.collections.Maps.newHashMap;

/**
 * Tests each endpoint of the V2 Rest Api through http calls
 */
public class RestControllerV2APIIT
{
	private static final Logger LOG = getLogger(RestControllerV2APIIT.class);

	private static final String REST_TEST_USER = "api_v2_test_user";
	private static final String REST_TEST_USER_PASSWORD = "api_v2_test_user_password";
	private static final String V2_TEST_FILE = "/RestControllerV2_API_TestEMX.xlsx";
	private static final String V2_DELETE_TEST_FILE = "/RestControllerV2_DeleteEMX.xlsx";
	private static final String V2_COPY_TEST_FILE = "/RestControllerV2_CopyEMX.xlsx";
	private static final String API_V2 = "api/v2/";

	private String testUserToken;
	private String adminToken;
	private String testUserId;

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Importing Test data");
		uploadEMX(adminToken, V2_TEST_FILE);
		uploadEMX(adminToken, V2_DELETE_TEST_FILE);
		uploadEMX(adminToken, V2_COPY_TEST_FILE);
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);

		grantSystemRights(adminToken, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_Attribute", WRITE);

		grantSystemRights(adminToken, testUserId, "sys_FileMeta", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_sec_Owned", READ);
		grantSystemRights(adminToken, testUserId, "sys_L10nString", WRITE);

		grantRights(adminToken, testUserId, "V2_API_TypeTestAPIV2", WRITE);
		grantRights(adminToken, testUserId, "V2_API_TypeTestRefAPIV2", WRITE);
		grantRights(adminToken, testUserId, "V2_API_LocationAPIV2", WRITE);
		grantRights(adminToken, testUserId, "V2_API_PersonAPIV2", WRITE);

		grantRights(adminToken, testUserId, "base_v2APITest1", WRITEMETA);
		grantRights(adminToken, testUserId, "base_v2APITest2", WRITEMETA);

		grantRights(adminToken, testUserId, "base_APICopyTest", WRITEMETA);

		testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

	@Test(enabled = false) // TODO
	public void testGetVersion()
	{
		//	@Autowired
		//	@RequestMapping(value = "/version", method = GET)
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
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "base_v2APITest1")
			   .then()
			   .log()
			   .all()
			   .body("total", equalTo(4), "items[0].value", equalTo("ref2"), "items[1].value", equalTo("ref3"),
					   "items[2].value", equalTo("ref4"), "items[3].value", equalTo("ref5"));
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
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "base_v2APITest2")
			   .then()
			   .log()
			   .all()
			   .body("total", equalTo(1), "items[0].value", equalTo("ref5"));
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
		//		@RequestMapping(value = "/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
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
			   .body("location", equalTo("/api/v2/V2_API_TypeTestv2?q=id=in=(\"55\",\"57\")"), "resources[0].href",
					   equalTo("/api/v2/V2_API_TypeTestAPIV2/55"), "resources[1].href",
					   equalTo("/api/v2/V2_API_TypeTestAPIV2/57"));
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
			   .body("href", equalTo("/api/v2/base_CopiedEntity"), "items[0].label", equalTo("Copied!"));
	}

	@Test(enabled = false) // FIXME
	public void testUpdateEntities()
	{
		//		@RequestMapping(value = "/{entityName}", method = PUT)
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
		//	@RequestMapping(value = "/{entityName}/{attributeName}", method = PUT)
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
			   .body("form_number_control_placeholder", equalTo("Number"), "form_bool_true", equalTo("Yes"),
					   "form_xref_control_placeholder", equalTo("Search for a Value"), "form_date_control_placeholder",
					   equalTo("Date"), "form_url_control_placeholder", equalTo("URL"),
					   "form_computed_control_placeholder", equalTo("This value is computed automatically"),
					   "form_email_control_placeholder", equalTo("Email"), "form_bool_false", equalTo("No"),
					   "form_bool_missing", equalTo("N/A"), "form_mref_control_placeholder",
					   equalTo("Search for Values"));
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
			   .body("items[0].msgid", equalTo("my_test_key"), "items[0].namespace", equalTo("apiv2test"));
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
			   .statusCode(NO_CONTENT);

		given().log()
			   .all()
			   .header(X_MOLGENIS_TOKEN, testUserToken)
			   .get(API_V2 + "sys_L10nString?q=namespace==apiv2test")
			   .then()
			   .log()
			   .all()
			   .body("total", equalTo(0));
	}

	private void validateRetrieveEntityWithoutAttributeFilter(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("_meta.href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.hrefCollection",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.name", equalTo("V2_API_TypeTestRefAPIV2"),
				"_meta.label", equalTo("TypeTestRefAPIV2"), "_meta.description",
				equalTo("MOLGENIS Data types test ref entity"), "_meta.attributes[0].href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"), "_meta.attributes[0].fieldType",
				equalTo("STRING"), "_meta.attributes[0].name", equalTo("value"), "_meta.attributes[0].label",
				equalTo("value label"), "_meta.attributes[0].description", equalTo("TypeTestRef value attribute"),
				"_meta.attributes[0].attributes", equalTo(newArrayList()), "_meta.attributes[0].maxLength",
				equalTo(255), "_meta.attributes[0].auto", equalTo(false), "_meta.attributes[0].nillable",
				equalTo(false), "_meta.attributes[0].readOnly", equalTo(true), "_meta.attributes[0].labelAttribute",
				equalTo(false), "_meta.attributes[0].unique", equalTo(true), "_meta.attributes[0].visible",
				equalTo(true), "_meta.attributes[0].lookupAttribute", equalTo(true),
				"_meta.attributes[0].isAggregatable", equalTo(false), "_meta.attributes[1].href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"), "_meta.attributes[1].fieldType",
				equalTo("STRING"), "_meta.attributes[1].name", equalTo("label"), "_meta.attributes[1].label",
				equalTo("label label"), "_meta.attributes[1].description", equalTo("TypeTestRef label attribute"),
				"_meta.attributes[1].attributes", equalTo(newArrayList()), "_meta.attributes[1].maxLength",
				equalTo(255), "_meta.attributes[1].auto", equalTo(false), "_meta.attributes[1].nillable",
				equalTo(false), "_meta.attributes[1].readOnly", equalTo(false), "_meta.attributes[1].labelAttribute",
				equalTo(true), "_meta.attributes[1].unique", equalTo(false), "_meta.attributes[1].visible",
				equalTo(true), "_meta.attributes[1].lookupAttribute", equalTo(true),
				"_meta.attributes[1].isAggregatable", equalTo(false), "_meta.labelAttribute", equalTo("label"),
				"_meta.idAttribute", equalTo("value"), "_meta.lookupAttributes",
				equalTo(newArrayList("value", "label")), "_meta.isAbstract", equalTo(false), "_meta.writable",
				equalTo(true), "_meta.languageCode", equalTo("en"), "_href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "value", equalTo("ref1"), "label", equalTo("label1"));
	}

	private void validateRetrieveEntityWithAttributeFilter(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("_meta.href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.hrefCollection",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "_meta.name", equalTo("V2_API_TypeTestRefAPIV2"),
				"_meta.label", equalTo("TypeTestRefAPIV2"), "_meta.description",
				equalTo("MOLGENIS Data types test ref entity"), "_meta.attributes[0].href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"), "_meta.attributes[0].fieldType",
				equalTo("STRING"), "_meta.attributes[0].name", equalTo("label"), "_meta.attributes[0].label",
				equalTo("label label"), "_meta.attributes[0].description", equalTo("TypeTestRef label attribute"),
				"_meta.attributes[0].attributes", equalTo(newArrayList()), "_meta.attributes[0].maxLength",
				equalTo(255), "_meta.attributes[0].auto", equalTo(false), "_meta.attributes[0].nillable",
				equalTo(false), "_meta.attributes[0].readOnly", equalTo(false), "_meta.attributes[0].labelAttribute",
				equalTo(true), "_meta.attributes[0].unique", equalTo(false), "_meta.attributes[0].visible",
				equalTo(true), "_meta.attributes[0].lookupAttribute", equalTo(true),
				"_meta.attributes[0].isAggregatable", equalTo(false), "_meta.labelAttribute", equalTo("label"),
				"_meta.idAttribute", equalTo("value"), "_meta.lookupAttributes",
				equalTo(newArrayList("value", "label")), "_meta.isAbstract", equalTo(false), "_meta.writable",
				equalTo(true), "_meta.languageCode", equalTo("en"), "_href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "label", equalTo("label1"));
	}

	private void validateRetrieveEntityCollection(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.hrefCollection",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2"), "meta.name", equalTo("V2_API_TypeTestRefAPIV2"),
				"meta.label", equalTo("TypeTestRefAPIV2"), "meta.description",
				equalTo("MOLGENIS Data types test ref entity"), "meta.attributes[0].href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"), "meta.attributes[0].fieldType",
				equalTo("STRING"), "meta.attributes[0].name", equalTo("value"), "meta.attributes[0].label",
				equalTo("value label"), "meta.attributes[0].description", equalTo("TypeTestRef value attribute"),
				"meta.attributes[0].attributes", equalTo(newArrayList()), "meta.attributes[0].maxLength", equalTo(255),
				"meta.attributes[0].auto", equalTo(false), "meta.attributes[0].nillable", equalTo(false),
				"meta.attributes[0].readOnly", equalTo(true), "meta.attributes[0].labelAttribute", equalTo(false),
				"meta.attributes[0].unique", equalTo(true), "meta.attributes[0].visible", equalTo(true),
				"meta.attributes[0].lookupAttribute", equalTo(true), "meta.attributes[0].isAggregatable",
				equalTo(false), "meta.attributes[1].href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/label"),
				"meta.attributes[1].fieldType", equalTo("STRING"), "meta.attributes[1].name", equalTo("label"),
				"meta.attributes[1].label", equalTo("label label"), "meta.attributes[1].description",
				equalTo("TypeTestRef label attribute"), "meta.attributes[1].attributes", equalTo(newArrayList()),
				"meta.attributes[1].maxLength", equalTo(255), "meta.attributes[1].auto", equalTo(false),
				"meta.attributes[1].nillable", equalTo(false), "meta.attributes[1].readOnly", equalTo(false),
				"meta.attributes[1].labelAttribute", equalTo(true), "meta.attributes[1].unique", equalTo(false),
				"meta.attributes[1].visible", equalTo(true), "meta.attributes[1].lookupAttribute", equalTo(true),
				"meta.labelAttribute", equalTo("label"), "meta.idAttribute", equalTo("value"), "meta.lookupAttributes",
				equalTo(newArrayList("value", "label")), "meta.isAbstract", equalTo(false), "meta.writable",
				equalTo(true), "meta.languageCode", equalTo("en"), "start", equalTo(0), "num", equalTo(100), "total",
				equalTo(5), "items[0]._href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref1"), "items[0].value",
				equalTo("ref1"), "items[0].label", equalTo("label1"), "items[1]._href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref2"), "items[1].value", equalTo("ref2"), "items[1].label",
				equalTo("label2"), "items[2]._href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref3"), "items[2].value",
				equalTo("ref3"), "items[2].label", equalTo("label3"), "items[3]._href",
				equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref4"), "items[3].value", equalTo("ref4"), "items[3].label",
				equalTo("label4"), "items[4]._href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2/ref5"), "items[4].value",
				equalTo("ref5"), "items[4].label", equalTo("label5"));
	}

	private void validateRetrieveEntityAttributeMeta(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("href", equalTo("/api/v2/V2_API_TypeTestRefAPIV2/meta/value"), "fieldType", equalTo("STRING"),
				"name", equalTo("value"), "label", equalTo("value label"), "description",
				equalTo("TypeTestRef value attribute"), "attributes", equalTo(newArrayList()), "maxLength",
				equalTo(255), "auto", equalTo(false), "nillable", equalTo(false), "readOnly", equalTo(true),
				"labelAttribute", equalTo(false), "unique", equalTo(true), "visible", equalTo(true), "lookupAttribute",
				equalTo(true), "isAggregatable", equalTo(false));
	}

	private void validateGetI18nStrings(ValidatableResponse response)
	{
		response.statusCode(OKE);
		response.body("dataexplorer_wizard_cancel", equalTo("Cancel"), "questionnaires_table_view_questionnaire_button",
				equalTo("View questionnaire"), "dataexplorer_aggregates_title", equalTo("Aggregates"),
				"dataexplorer_directory_export_no_filters",
				equalTo("Please filter the collections before sending a request to the negotiator."),
				"questionnaire_submit", equalTo("Submit"), "questionnaires_title", equalTo("My questionnaires"),
				"form_bool_false", equalTo("No"), "form_bool_missing", equalTo("N/A"), "form_mref_control_placeholder",
				equalTo("Search for Values"), "dataexplorer_directory_export_dialog_message",
				equalTo("Your current selection of biobanks along with your filtering criteria will be sent to the BBMRI Negotiator. Are you sure?"),
				"questionnaires_table_status_header", equalTo("Status"), "dataexplorer_directory_export_dialog_yes",
				equalTo("Yes, Send to Negotiator"), "dataexplorer_wizard_button", equalTo("Wizard"),
				"dataexplorer_wizard_apply", equalTo("Apply"), "questionnaires_table_status_open", equalTo("Open"),
				"questionnaire_thank_you_page_back_button", equalTo("Back to My questionnaires"), "form_bool_true",
				equalTo("Yes"), "form_xref_control_placeholder", equalTo("Search for a Value"),
				"form_url_control_placeholder", equalTo("URL"), "questionnaires_table_continue_questionnaire_button",
				equalTo("Continue questionnaire"), "questionnaires_table_questionnaire_header",
				equalTo("Questionnaire"), "dataexplorer_aggregates_missing", equalTo("N/A"),
				"questionnaires_description", equalTo("Submitted and open questionnaires"),
				"questionnaires_table_status_submitted", equalTo("Submitted"), "questionnaire_save_and_continue",
				equalTo("Save and continue later"), "dataexplorer_directory_export_button",
				equalTo("Go to sample / data negotiation"), "dataexplorer_data_data_item_filters",
				equalTo("Data item filters"), "dataexplorer_aggregates_total", equalTo("Total"),
				"dataexplorer_directory_export_dialog_title", equalTo("Send request to the BBMRI Negotiator?"),
				"questionnaires_table_status_not_started", equalTo("Not started yet"), "form_email_control_placeholder",
				equalTo("Email"), "form_computed_control_placeholder", equalTo("This value is computed automatically"),
				"dataexplorer_aggregates_no_result_message", equalTo("No results found"),
				"questionnaires_table_start_questionnaire_button", equalTo("Start questionnaire"),
				"dataexplorer_wizard_title", equalTo("Filter Wizard"), "dataexplorer_aggregates_distinct",
				equalTo("Distinct"), "form_number_control_placeholder", equalTo("Number"),
				"dataexplorer_aggregates_group_by", equalTo("Group by"), "dataexplorer_directory_export_dialog_no",
				equalTo("No, I want to keep filtering"), "questionnaire_back_button",
				equalTo("Back to my questionnaires"), "form_date_control_placeholder", equalTo("Date"),
				"questionnaires_no_questionnaires_found_message", equalTo("No questionnaires found"));
	}

	@AfterClass
	public void afterClass()
	{
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
		given().header(X_MOLGENIS_TOKEN, testUserToken).when().post("api/v1/logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, adminToken).when().delete("api/v1/sys_sec_User/" + testUserId);
	}

}


