package org.molgenis.api.tests.utils;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.io.Resources;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.molgenis.core.ui.admin.permission.PermissionManagerController;
import org.slf4j.Logger;

/** Methods shared by Rest Api tests */
public class RestTestUtils {
  private static final Logger LOG = getLogger(RestTestUtils.class);

  public static final String APPLICATION_JSON = "application/json";
  public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
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
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;

  public enum Permission {
    READ,
    WRITE,
    COUNT,
    WRITEMETA,
    NONE
  }

  /**
   * Login with user name and password and return token on success.
   *
   * @param username the username to login with
   * @param password the password to use for login
   * @return the token returned from the login
   */
  public static String login(String username, String password) {
    JSONObject loginBody = new JSONObject();
    loginBody.put("username", username);
    loginBody.put("password", password);

    return given()
        .contentType(APPLICATION_JSON)
        .body(loginBody.toJSONString())
        .when()
        .post("api/v1/login")
        .then()
        .extract()
        .path("token");
  }

  /**
   * Create a user with username and password as admin using given token.
   *
   * @param adminToken the token to use for login
   * @param username the name of the user to create
   * @param password the password of the user to create
   */
  public static void createUser(String adminToken, String username, String password) {
    if (adminToken != null) {
      JSONObject createTestUserBody = new JSONObject();
      createTestUserBody.put("active", true);
      createTestUserBody.put("username", username);
      createTestUserBody.put("password_", password);
      createTestUserBody.put("superuser", false);
      createTestUserBody.put("changePassword", false);
      createTestUserBody.put("Email", username + "@example.com");

      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(APPLICATION_JSON)
          .body(createTestUserBody.toJSONString())
          .when()
          .post("api/v1/sys_sec_User");
    }
  }

  public static void createPackage(String adminToken, String packageId) {
    JSONObject createPackageBody = new JSONObject();
    createPackageBody.put("id", packageId);
    createPackageBody.put("label", packageId);

    given()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .body(createPackageBody.toJSONString())
        .when()
        .post("api/v1/sys_md_Package");
  }

  /**
   * Import emx file using add/update.
   *
   * <p>Importing is done async in the backend, but this methods waits for importing to be done.
   *
   * @param adminToken to use for login
   * @param fileName the file to upload
   * @return String indicating state of completed job
   */
  public static String uploadEMX(String adminToken, String fileName) {
    File file = getResourceFile(fileName);

    return uploadEMXFile(adminToken, file);
  }

  /**
   * Import emx file using add/update.
   *
   * <p>Importing is done async in the backend, but this methods waits for importing to be done.
   *
   * @param adminToken to use for login
   * @param pathToFileFolder path to folder to look for emx file to import
   * @param fileName name of the file to upload
   * @return String indicating state of completed job
   */
  public static String uploadEMX(String adminToken, String pathToFileFolder, String fileName) {
    File file = new File(pathToFileFolder + File.separator + fileName);

    return uploadEMXFile(adminToken, file);
  }

  private static String uploadEMXFile(String adminToken, File file) {
    String importJobStatusUrl =
        given()
            .multiPart(file)
            .param("file")
            .param("action", "ADD_UPDATE_EXISTING")
            .param("packageId", "base")
            .header(X_MOLGENIS_TOKEN, adminToken)
            .post("plugin/importwizard/importFile")
            .then()
            .statusCode(CREATED)
            .extract()
            .header("Location");

    return monitorImportJob(adminToken, importJobStatusUrl);
  }

  private static File getResourceFile(String fileName) {
    URL resourceUrl = Resources.getResource(RestTestUtils.class, fileName);
    File file = null;
    try {
      file = new File(new URI(resourceUrl.toString()).getPath());
    } catch (URISyntaxException e) {
      LOG.error(e.getMessage());
    }
    return file;
  }

  /**
   * Import vcf file using add
   *
   * <p>Importing is done async in the backend, but this methods waits for importing to be done.
   *
   * @param adminToken to use for login
   * @param fileName the file to upload
   * @return String indicating state of completed job
   */
  public static String uploadVCFToEntity(String adminToken, String fileName, String entityName) {
    File file = getResourceFile(fileName);

    String importJobStatusUrl =
        given()
            .multiPart(file)
            .param("file")
            .param("action", "ADD")
            .param("entityName", entityName)
            .header(X_MOLGENIS_TOKEN, adminToken)
            .post("plugin/importwizard/importFile")
            .then()
            .extract()
            .header("Location");
    return monitorImportJob(adminToken, importJobStatusUrl);
  }

  /**
   * Import vcf file using add
   *
   * <p>Importing is done async in the backend, but this methods waits for importing to be done.
   *
   * @param adminToken to use for login
   * @param pathToFileFolder path to folder to look for emx file to import
   * @param fileName the file to upload
   * @return String indicating state of completed job
   */
  public static String uploadVCF(String adminToken, String pathToFileFolder, String fileName) {
    File file = new File(pathToFileFolder + File.separator + fileName);

    String importJobStatusUrl =
        given()
            .multiPart(file)
            .param("file")
            .param("action", "ADD")
            .header(X_MOLGENIS_TOKEN, adminToken)
            .post("plugin/importwizard/importFile")
            .then()
            .extract()
            .header("Location");
    return monitorImportJob(adminToken, importJobStatusUrl);
  }

  /** Given the job uri and token, wait until the job is done and report back the status. */
  private static String monitorImportJob(String adminToken, String importJobURL) {
    LOG.info("############ " + importJobURL);
    await()
        .pollDelay(500, MILLISECONDS)
        .atMost(5, MINUTES)
        .until(() -> pollForStatus(adminToken, importJobURL), not(equalTo("RUNNING")));
    LOG.info("Import completed");
    return pollForStatus(adminToken, importJobURL);
  }

  private static String pollForStatus(String adminToken, String importJobURL) {
    return given()
        .contentType(APPLICATION_JSON)
        .header(X_MOLGENIS_TOKEN, adminToken)
        .get(importJobURL)
        .then()
        .statusCode(OKE)
        .extract()
        .path("status")
        .toString();
  }

  /**
   * Reads the contents of a file and stores it in a String. Useful if you want to compare the
   * contents of a file with the response of an API endpoint, if that endpoint returns a file (e.g.
   * /api/v1/csv/{entityName}.
   *
   * @param fileName the name of the file
   * @return a string containing the files contents
   */
  public static String getFileContents(String fileName) {
    URL resourceUrl = Resources.getResource(RestTestUtils.class, fileName);
    File file;
    try {
      file = new File(new URI(resourceUrl.toString()).getPath());
      return readFileToString(file);
    } catch (Exception e) {
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
  public static JSONObject readJsonFile(String fileName) {
    JSONObject jsonObject = null;
    try {
      jsonObject =
          (JSONObject)
              new JSONParser(JSONParser.MODE_JSON_SIMPLE)
                  .parse(
                      new InputStreamReader(
                          RestTestUtils.class.getResourceAsStream(fileName),
                          Charset.forName("UTF-8")));

    } catch (ParseException e) {
      LOG.error("Unable to readJsonFile(" + fileName + ")");
      LOG.error(e.getMessage());
    }

    return jsonObject;
  }

  /**
   * Grant user read rights on given plugins
   *
   * @param adminToken the token to use for signin
   * @param username the ID (not the name) of the user that needs to get the rights
   * @param plugins the IDs of the plugins the user should be able to read
   */
  public static void setGrantedPluginPermissions(
      String adminToken, String username, String... plugins) {
    Map<String, String> pluginParams =
        stream(plugins).collect(toMap(pluginId -> "radio-" + pluginId, pluginId -> "READ"));

    given()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(X_WWW_FORM_URLENCODED)
        .params(pluginParams)
        .param("username", username)
        .when()
        .post(PermissionManagerController.URI + "/update/plugin/user")
        .then()
        .statusCode(OKE);
  }

  /**
   * Sets user permissions on repositories. Existing repository permissions will be removed.
   *
   * @param adminToken the token to use for authentication
   * @param username the name of the user to grant the permissions to
   * @param permissions Map mapping entity type ID to permission to grant
   */
  public static void setGrantedRepositoryPermissions(
      String adminToken, String username, Map<String, Permission> permissions) {
    if (adminToken != null) {
      Map<String, String> params =
          permissions
              .entrySet()
              .stream()
              .collect(
                  toMap(
                      entry -> "radio-" + entry.getKey(),
                      entry -> entry.getValue().name().toLowerCase()));

      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(X_WWW_FORM_URLENCODED)
          .params(params)
          .param("username", username)
          .when()
          .log()
          .all()
          .post(PermissionManagerController.URI + "/update/entityclass/user")
          .then()
          .log()
          .all()
          .statusCode(OKE);
    }
  }

  /**
   * Sets user permissions on repositories. Existing repository permissions will be removed.
   *
   * @param adminToken the token to use for authentication
   * @param username the id of the user to grant the permissions to
   * @param permissions Map mapping entity type ID to permission to grant
   */
  public static void setGrantedPackagePermissions(
      String adminToken, String username, Map<String, Permission> permissions) {
    if (adminToken != null) {
      Map<String, String> params =
          permissions
              .entrySet()
              .stream()
              .collect(
                  toMap(
                      entry -> "radio-" + entry.getKey(),
                      entry -> entry.getValue().name().toLowerCase()));

      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(X_WWW_FORM_URLENCODED)
          .params(params)
          .param("username", username)
          .when()
          .log()
          .all()
          .post(PermissionManagerController.URI + "/update/package/user")
          .then()
          .log()
          .all()
          .statusCode(OKE);
    }
  }

  public static void removePackages(String adminToken, List<String> packageNames) {
    packageNames.forEach(packageName -> removePackage(adminToken, packageName));
  }

  private static void removePackage(String adminToken, String packageName) {
    if (adminToken != null && packageName != null) {
      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(APPLICATION_JSON)
          .delete("api/v1/sys_md_Package/" + packageName);
    }
  }

  public static void removeEntities(String adminToken, List<String> entities) {
    entities.forEach(entity -> removeEntity(adminToken, entity));
  }

  public static void removeEntity(String adminToken, String entityId) {
    if (adminToken != null && entityId != null) {
      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(APPLICATION_JSON)
          .delete("api/v1/" + entityId + "/meta");
    }
  }

  public static void removeEntityFromTable(
      String adminToken, String entityTypeId, String entityId) {
    if (adminToken != null && entityTypeId != null && entityId != null) {
      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(APPLICATION_JSON)
          .delete("api/v1/" + entityTypeId + "/" + entityId);
    }
  }

  public static void removeImportJobs(String adminToken, List<String> jobIds) {
    jobIds.forEach(jobId -> removeImportJob(adminToken, jobId));
  }

  private static void removeImportJob(String adminToken, String jobId) {
    if (adminToken != null && jobId != null) {
      given()
          .header(X_MOLGENIS_TOKEN, adminToken)
          .contentType(APPLICATION_JSON)
          .delete("api/v2/sys_job_OneClickImportJobExecution/" + jobId);
    }
  }

  /** Removes all permissions for a given user identifier */
  public static void removeRightsForUser(String adminToken, String username) {
    if (adminToken != null && username != null) {
      // TODO: no api to revoke permissions currently
    }
  }

  /** Removes the token for the test user by logging out */
  public static void cleanupUserToken(String testUserToken) {
    if (testUserToken != null) {
      given().header(X_MOLGENIS_TOKEN, testUserToken).when().post("api/v1/logout");
    }
  }
}
