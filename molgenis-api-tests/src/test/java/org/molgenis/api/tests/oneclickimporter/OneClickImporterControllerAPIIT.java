package org.molgenis.api.tests.oneclickimporter;

import static com.google.common.io.Resources.getResource;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.molgenis.api.tests.utils.RestTestUtils.DEFAULT_ADMIN_NAME;
import static org.molgenis.api.tests.utils.RestTestUtils.DEFAULT_ADMIN_PW;
import static org.molgenis.api.tests.utils.RestTestUtils.DEFAULT_HOST;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.READ;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITE;
import static org.molgenis.api.tests.utils.RestTestUtils.Permission.WRITEMETA;
import static org.molgenis.api.tests.utils.RestTestUtils.X_MOLGENIS_TOKEN;
import static org.molgenis.api.tests.utils.RestTestUtils.cleanupUserToken;
import static org.molgenis.api.tests.utils.RestTestUtils.createUser;
import static org.molgenis.api.tests.utils.RestTestUtils.login;
import static org.molgenis.api.tests.utils.RestTestUtils.removeEntities;
import static org.molgenis.api.tests.utils.RestTestUtils.removeImportJobs;
import static org.molgenis.api.tests.utils.RestTestUtils.removePackages;
import static org.molgenis.api.tests.utils.RestTestUtils.removeRightsForUser;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedPackagePermissions;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedPluginPermissions;
import static org.molgenis.api.tests.utils.RestTestUtils.setGrantedRepositoryPermissions;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertTrue;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.restassured.internal.ValidatableResponseImpl;
import io.restassured.response.ValidatableResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.molgenis.api.tests.rest.v2.RestControllerV2APIIT;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.oneclickimporter.controller.OneClickImporterController;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class OneClickImporterControllerAPIIT {
  private static final Logger LOG = getLogger(OneClickImporterControllerAPIIT.class);

  private String oneClickImporterTestUsername;
  private static final String ONE_CLICK_IMPORTER_TEST_USER_PASSWORD =
      "one_click_importer_test_user_password";
  private static final String API_V2 = "api/v2/";

  private static final String ONE_CLICK_IMPORT_EXCEL_FILE = "/OneClickImport_complex-valid.xlsx";
  private static final String ONE_CLICK_IMPORT_CSV_FILE = "/OneClickImport_complex-valid.csv";

  private String testUserToken;
  private String adminToken;

  // Fields to store created entity ids from import test, used during cleanup to remove the entities
  private List<String> importedEntities = new ArrayList<>();
  private List<String> importPackages = new ArrayList<>();
  private List<String> importJobIds = new ArrayList<>();

  @BeforeClass
  public void beforeClass() {
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

    RestTestUtils.createPackage(adminToken, "base");

    oneClickImporterTestUsername = "one_click_importer_test_user" + System.currentTimeMillis();
    createUser(adminToken, oneClickImporterTestUsername, ONE_CLICK_IMPORTER_TEST_USER_PASSWORD);

    setGrantedRepositoryPermissions(
        adminToken,
        oneClickImporterTestUsername,
        ImmutableMap.<String, Permission>builder()
            .put("sys_md_Package", WRITE)
            .put("sys_md_EntityType", WRITE)
            .put("sys_md_Attribute", WRITE)
            .put("sys_FileMeta", WRITE)
            .put("sys_L10nString", WRITE)
            .put("sys_job_JobExecution", READ)
            .put("sys_job_OneClickImportJobExecution", WRITE)
            .build());
    setGrantedPackagePermissions(
        adminToken,
        oneClickImporterTestUsername,
        ImmutableMap.<String, Permission>builder().put("base", WRITEMETA).build());
    setGrantedPluginPermissions(adminToken, oneClickImporterTestUsername, "one-click-importer");

    testUserToken = login(oneClickImporterTestUsername, ONE_CLICK_IMPORTER_TEST_USER_PASSWORD);
  }

  @Test
  public void testOneClickImportExcelFile() throws IOException, URISyntaxException {
    oneClickImportTest(ONE_CLICK_IMPORT_EXCEL_FILE);
  }

  @Test
  public void testOneClickImportCsvFile() throws IOException, URISyntaxException {
    oneClickImportTest(ONE_CLICK_IMPORT_CSV_FILE);
  }

  private void oneClickImportTest(String fileToImport) throws URISyntaxException, IOException {
    URL resourceUrl = getResource(RestControllerV2APIIT.class, fileToImport);
    File file = new File(new URI(resourceUrl.toString()).getPath());

    // Post the file to be imported
    ValidatableResponse response =
        given()
            .log()
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

    String jobStatus =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, testUserToken)
            .get(jobUrl)
            .then()
            .log()
            .all()
            .statusCode(OKE)
            .extract()
            .path("status");

    List<String> validJobStats = Arrays.asList("PENDING", "RUNNING", "SUCCESS");
    assertTrue(validJobStats.contains(jobStatus));

    await()
        .pollDelay(500, MILLISECONDS)
        .atMost(3, MINUTES)
        .until(() -> pollJobForStatus(jobUrl), not(is("PENDING")));
    await()
        .pollDelay(500, MILLISECONDS)
        .atMost(10, SECONDS)
        .until(() -> pollJobForStatus(jobUrl), is("SUCCESS"));

    // Extract the id of the entity created by the import
    ValidatableResponse completedJobResponse =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, testUserToken)
            .get(jobUrl)
            .then()
            .statusCode(OKE);

    JsonArray entityTypeId =
        new Gson()
            .fromJson(
                completedJobResponse.extract().jsonPath().get("entityTypes").toString(),
                JsonArray.class);
    String entityId = entityTypeId.get(0).getAsJsonObject().get("id").getAsString();
    String packageName = completedJobResponse.extract().path("package");
    String jobId = completedJobResponse.extract().path("identifier");

    // Store to use during cleanup
    importedEntities.add(entityId);
    importPackages.add(packageName);
    importJobIds.add(jobId);

    // Get the entity value to check the import
    ValidatableResponse entityResponse =
        given()
            .log()
            .all()
            .header(X_MOLGENIS_TOKEN, testUserToken)
            .get(API_V2 + entityId + "?attrs=~id,first_name,last_name,full_name,UMCG_employee,Age")
            .then()
            .log()
            .all();
    entityResponse.statusCode(OKE);

    JSONAssert.assertEquals(
        Files.readFile(getClass().getResourceAsStream("users.json")),
        entityResponse.extract().body().asString(),
        false);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    // Clean up created entities
    removeEntities(adminToken, importedEntities);

    // Clean up created packages
    removePackages(adminToken, importPackages);

    // Clean up jobs
    removeImportJobs(adminToken, importJobIds);

    // Clean up permissions
    removeRightsForUser(adminToken, oneClickImporterTestUsername);

    // Clean up Token for user
    cleanupUserToken(testUserToken);
  }

  private String pollJobForStatus(String jobUrl) {
    String status =
        given().header(X_MOLGENIS_TOKEN, testUserToken).get(jobUrl).then().extract().path("status");
    LOG.info("Import job status : {}", status);
    return status;
  }
}
