package org.molgenis.api.tests.oneclickimporter;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.restassured.internal.ValidatableResponseImpl;
import io.restassured.response.ValidatableResponse;
import org.molgenis.api.tests.rest.v2.RestControllerV2APIIT;
import org.molgenis.oneclickimporter.controller.OneClickImporterController;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.molgenis.api.tests.utils.RestTestUtils.*;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertTrue;

public class OneClickImporterControllerAPIIT
{
	private static final Logger LOG = getLogger(OneClickImporterControllerAPIIT.class);

	private static final String ONE_CLICK_IMPORTER_TEST_USER = "one_click_importer_test_user";
	private static final String ONE_CLICK_IMPORTER_TEST_USER_PASSWORD = "one_click_importer_test_user_password";
	private static final String API_V2 = "api/v2/";

	private static final String ONE_CLICK_IMPORT_EXCEL_FILE = "/OneClickImport_complex-valid.xlsx";
	private static final String ONE_CLICK_IMPORT_CSV_FILE = "/OneClickImport_complex-valid.csv";

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
		baseURI = Strings.isNullOrEmpty(envHost) ? DEFAULT_HOST : envHost;
		LOG.info("baseURI: " + baseURI);

		String envAdminName = System.getProperty("REST_TEST_ADMIN_NAME");
		String adminUserName = Strings.isNullOrEmpty(envAdminName) ? DEFAULT_ADMIN_NAME : envAdminName;
		LOG.info("adminUserName: " + adminUserName);

		String envAdminPW = System.getProperty("REST_TEST_ADMIN_PW");
		String adminPassword = Strings.isNullOrEmpty(envAdminPW) ? DEFAULT_ADMIN_PW : envAdminPW;
		LOG.info("adminPassword: " + adminPassword);

		adminToken = login(adminUserName, adminPassword);
		createUser(adminToken, ONE_CLICK_IMPORTER_TEST_USER, ONE_CLICK_IMPORTER_TEST_USER_PASSWORD);
		testUserId = getUserId(adminToken, ONE_CLICK_IMPORTER_TEST_USER);

		grantSystemRights(adminToken, testUserId, "sys_md_Package", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_EntityType", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_md_Attribute", WRITE);

		grantSystemRights(adminToken, testUserId, "sys_FileMeta", WRITE);
		grantSystemRights(adminToken, testUserId, "sys_sec_Owned", READ);
		grantSystemRights(adminToken, testUserId, "sys_L10nString", WRITE);

		grantPluginRights(adminToken, testUserId, "one-click-importer");
		grantSystemRights(adminToken, testUserId, "sys_job_JobExecution", READ);
		grantSystemRights(adminToken, testUserId, "sys_job_OneClickImportJobExecution", READ);

		testUserToken = login(ONE_CLICK_IMPORTER_TEST_USER, ONE_CLICK_IMPORTER_TEST_USER_PASSWORD);
	}

	@Test
	public void testOneClickImportExcelFile() throws IOException, URISyntaxException
	{
		oneClickImportTest(ONE_CLICK_IMPORT_EXCEL_FILE);
	}

	@Test
	public void testOneClickImportCsvFile() throws IOException, URISyntaxException
	{
		oneClickImportTest(ONE_CLICK_IMPORT_CSV_FILE);
	}

	private void oneClickImportTest(String fileToImport) throws URISyntaxException
	{
		URL resourceUrl = getResource(RestControllerV2APIIT.class, fileToImport);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		// Post the file to be imported
		ValidatableResponse response = given().log()
											  .all()
											  .header(X_MOLGENIS_TOKEN, testUserToken)
											  .multiPart(file)
											  .post(OneClickImporterController.URI + "/upload")
											  .then()
											  .log()
											  .all()
											  .statusCode(OKE);

		// Verify the post returns a job url
		String jobUrl = ((ValidatableResponseImpl) response).originalResponse().asString();
		assertTrue(jobUrl.startsWith("/api/v2/sys_job_OneClickImportJobExecution/"));

		String jobStatus = given().log()
								  .all()
								  .header(X_MOLGENIS_TOKEN, testUserToken)
								  .get(jobUrl)
								  .then()
								  .statusCode(OKE)
								  .extract()
								  .path("status");

		List<String> validJobStats = Arrays.asList("PENDING", "RUNNING", "SUCCESS");
		assertTrue(validJobStats.contains(jobStatus));

		await().pollDelay(500, MILLISECONDS)
			   .atMost(30, SECONDS)
			   .until(() -> pollJobForStatus(jobUrl), equalTo("SUCCESS"));

		// Extract the id of the entity created by the import
		ValidatableResponse completedJobResponse = given().log()
														  .all()
														  .header(X_MOLGENIS_TOKEN, testUserToken)
														  .get(jobUrl)
														  .then()
														  .statusCode(OKE);

		JsonArray entityTypeId = new Gson().fromJson(
				completedJobResponse.extract().jsonPath().get("entityTypes").toString(), JsonArray.class);
		String entityId = entityTypeId.get(0).getAsJsonObject().get("id").getAsString();
		String packageName = completedJobResponse.extract().path("package");
		String jobId = completedJobResponse.extract().path("identifier");

		// Store to use during cleanup
		importedEntities.add(entityId);
		importPackages.add(packageName);
		importJobIds.add(jobId);

		// Get the entity value to check the import
		ValidatableResponse entityResponse = given().log()
													.all()
													.header(X_MOLGENIS_TOKEN, testUserToken)
													.get(API_V2 + entityId
															+ "?attrs=~id,first_name,last_name,full_name,UMCG_employee,Age")
													.then()
													.log()
													.all();
		entityResponse.statusCode(OKE);

		//TODO: Fix broken test
//		// Check first row for expected values
//		entityResponse.body("items[0].first_name", equalTo("Mark"));
//		entityResponse.body("items[0].last_name", equalTo("de Haan"));
//		entityResponse.body("items[0].full_name", equalTo("Mark de Haan"));
//		entityResponse.body("items[0].UMCG_employee", equalTo(true));
//		entityResponse.body("items[0].Age", equalTo(26));
//
//		// Check last row for expected values
//		entityResponse.body("items[9].first_name", equalTo("Jan"));
//		entityResponse.body("items[9].UMCG_employee", equalTo(false));
//		entityResponse.body("items[9].Age", equalTo(32));
	}

	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		// Clean up created entities
		removeEntities(adminToken, importedEntities);

		// Clean up created packages
		removePackages(adminToken, importPackages);

		// Clean up jobs
		removeImportJobs(adminToken, importJobIds);

		// Clean up permissions
		removeRightsForUser(adminToken, testUserId);

		// Clean up Token for user
		cleanupUserToken(testUserToken);

		// Clean up user
		cleanupUser(adminToken, testUserId);
	}

	private String pollJobForStatus(String jobUrl)
	{
		String status = given().header(X_MOLGENIS_TOKEN, testUserToken).get(jobUrl).then().extract().path("status");
		LOG.info("Import job status : {}", status);
		return status;
	}

}
