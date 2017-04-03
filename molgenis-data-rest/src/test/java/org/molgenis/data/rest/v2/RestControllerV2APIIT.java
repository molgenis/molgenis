package org.molgenis.data.rest.v2;

import io.restassured.RestAssured;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.molgenis.data.rest.RestControllerIT.Permission.WRITE;
import static org.molgenis.data.rest.convert.RestTestUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tests each endpoint of the V2 Rest Api through http calls
 */
public class RestControllerV2APIIT
{
	private static final Logger LOG = getLogger(RestControllerV2APIIT.class);

	private static final String REST_TEST_USER = "api_test_user";
	private static final String REST_TEST_USER_PASSWORD = "api_test_user_password";
	private static final String V1_TEST_FILE = "/RestControllerV1_TestEMX.xlsx";
	private static final String PATH = "api/v2/";

	private static final String PACKAGE_PERMISSION_ID = "package_permission_ID";
	private static final String ENTITY_TYPE_PERMISSION_ID = "entityType_permission_ID";
	private static final String ATTRIBUTE_PERMISSION_ID = "attribute_permission_ID";
	private static final String FILE_META_PERMISSION_ID = "file_meta_permission_ID";
	private static final String OWNED_PERMISSION_ID = "owned_permission_ID";

	private static final String TYPE_TEST_PERMISSION_ID = "typeTest_permission_ID";
	private static final String TYPE_TEST_REF_PERMISSION_ID = "typeTestRef_permission_ID";
	private static final String LOCATION_PERMISSION_ID = "location_permission_ID";
	private static final String PERSONS_PERMISSION_ID = "persons_permission_ID";

	private String testUserToken;
	private String adminToken;
	private String testUserId;

	@BeforeClass
	public void beforeClass()
	{
		LOG.info("Read environment variables");
		String envHost = System.getProperty("REST_TEST_HOST");
		RestAssured.baseURI = Strings.isEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isEmpty(envHost) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);

		LOG.info("Importing Test data");
		uploadEMX(adminToken, V1_TEST_FILE);
		LOG.info("Importing Done");

		createUser(adminToken, REST_TEST_USER, REST_TEST_USER_PASSWORD);

		testUserId = getUserId(adminToken, REST_TEST_USER);
		LOG.info("testUserId: " + testUserId);

		grantSystemRights(adminToken, PACKAGE_PERMISSION_ID, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, ENTITY_TYPE_PERMISSION_ID, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, ATTRIBUTE_PERMISSION_ID, testUserId, "sys_md_Attribute", WRITE);

		grantRights(adminToken, TYPE_TEST_PERMISSION_ID, testUserId, "TypeTest", WRITE);
		grantRights(adminToken, TYPE_TEST_REF_PERMISSION_ID, testUserId, "TypeTestRef", WRITE);
		grantRights(adminToken, LOCATION_PERMISSION_ID, testUserId, "Location", WRITE);
		grantRights(adminToken, PERSONS_PERMISSION_ID, testUserId, "Person", WRITE);

		testUserToken = login(REST_TEST_USER, REST_TEST_USER_PASSWORD);
	}

//	@Autowired
//	@RequestMapping(value = "/version", method = GET)
//	@ResponseBody
//	public Map<String, String> getVersion(@Value("${molgenis.version:@null}") String molgenisVersion,
//			@Value("${molgenis.build.date:@null}") String molgenisBuildDate)
	@Test
	public void testGetVersion()
	{

	}

//	@RequestMapping(value = "/{entityName}/{id:.+}", method = GET)
//	@ResponseBody
//	public Map<String, Object> retrieveEntity(@PathVariable("entityName") String entityName,
//			@PathVariable("id") String untypedId,
//			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)

//	@RequestMapping(value = "/{entityName}/{id:.+}", method = POST, params = "_method=GET")
//	@ResponseBody
//	public Map<String, Object> retrieveEntityPost(@PathVariable("entityName") String entityName,
//			@PathVariable("id") String untypedId,
//			@RequestParam(value = "attrs", required = false) AttributeFilter attributeFilter)

//	@Transactional
//	@RequestMapping(value = "/{entityName}/{id:.+}", method = DELETE)
//	@ResponseStatus(NO_CONTENT)
//	public void deleteEntity(@PathVariable("entityName") String entityName, @PathVariable("id") String untypedId)

//	@RequestMapping(value = "/{entityName}", method = DELETE)
//	@ResponseStatus(NO_CONTENT)
//	public void deleteEntityCollection(@PathVariable("entityName") String entityName,
//			@RequestBody @Valid EntityCollectionDeleteRequestV2 request)

//	@RequestMapping(value = "/{entityName}", method = GET)
//	@ResponseBody
//	public EntityCollectionResponseV2 retrieveEntityCollection(@PathVariable("entityName") String entityName,
//			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest)

//	@RequestMapping(value = "/{entityName}", method = POST, params = "_method=GET")
//	@ResponseBody
//	public EntityCollectionResponseV2 retrieveEntityCollectionPost(@PathVariable("entityName") String entityName,
//			@Valid EntityCollectionRequestV2 request, HttpServletRequest httpRequest)

//	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = GET, produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public AttributeResponseV2 retrieveEntityAttributeMeta(@PathVariable("entityName") String entityName,
//			@PathVariable("attributeName") String attributeName)

//	@RequestMapping(value = "/{entityName}/meta/{attributeName}", method = POST, params = "_method=GET", produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public AttributeResponseV2 retrieveEntityAttributeMetaPost(@PathVariable("entityName") String entityName,
//			@PathVariable("attributeName") String attributeName)

//	@Transactional
//	@RequestMapping(value = "/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public EntityCollectionBatchCreateResponseBodyV2 createEntities(@PathVariable("entityName") String entityName,
//			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

//	@Transactional
//	@RequestMapping(value = "copy/{entityName}", method = POST, produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public String copyEntity(@PathVariable("entityName") String entityName,
//			@RequestBody @Valid CopyEntityRequestV2 request, HttpServletResponse response) throws Exception

//	@RequestMapping(value = "/{entityName}", method = PUT)
//		public synchronized void updateEntities(@PathVariable("entityName") String entityName,
//		@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

//	@RequestMapping(value = "/{entityName}/{attributeName}", method = PUT)
//	@ResponseStatus(OK)
//	public synchronized void updateAttribute(@PathVariable("entityName") String entityName,
//			@PathVariable("attributeName") String attributeName,
//			@RequestBody @Valid EntityCollectionBatchRequestV2 request, HttpServletResponse response) throws Exception

//	@RequestMapping(value = "/i18n", method = GET, produces = APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public Map<String, String> getI18nStrings()

	@AfterClass
	public void afterClass()
	{
		// Clean up TestEMX
		removeEntity(adminToken, "it_emx_datatypes_TypeTest");
		removeEntity(adminToken, "it_emx_datatypes_TypeTestRef");
		removeEntity(adminToken, "it_emx_datatypes_Location");
		removeEntity(adminToken, "it_emx_datatypes_Person");

		// Clean up permissions
		removeRight(adminToken, PACKAGE_PERMISSION_ID);
		removeRight(adminToken, ENTITY_TYPE_PERMISSION_ID);
		removeRight(adminToken, ATTRIBUTE_PERMISSION_ID);

		removeRight(adminToken, TYPE_TEST_PERMISSION_ID);
		removeRight(adminToken, TYPE_TEST_REF_PERMISSION_ID);
		removeRight(adminToken, LOCATION_PERMISSION_ID);
		removeRight(adminToken, PERSONS_PERMISSION_ID);

		// Clean up Token for user
		given().header(X_MOLGENIS_TOKEN, testUserToken).when().post(PATH + "logout");

		// Clean up user
		given().header(X_MOLGENIS_TOKEN, adminToken).when().delete("api/v1/sys_sec_User/" + testUserId);
	}


}
