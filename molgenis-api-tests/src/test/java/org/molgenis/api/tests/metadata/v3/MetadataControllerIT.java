package org.molgenis.api.tests.metadata.v3;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTest;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

@TestMethodOrder(OrderAnnotation.class)
class MetadataControllerIT extends AbstractApiTest {
  private static final Logger LOG = getLogger(MetadataControllerIT.class);

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTest.setUpBeforeClass();

    importData();
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    deleteData();

    AbstractApiTest.tearDownAfterClass();
  }

  private static void importData() {
    String importJobStatusUrl =
        given()
            .multiPart(getFile(MetadataControllerIT.class, "metadata_api_v3_emx.xlsx"))
            .param("file")
            .param("action", "ADD")
            .post("plugin/importwizard/importFile")
            .then()
            .statusCode(CREATED.value())
            .extract()
            .header("Location");

    monitorImportJob(importJobStatusUrl);
  }

  /** Given the job uri and token, wait until the job is done and report back the status. */
  private static String monitorImportJob(String importJobURL) {
    LOG.info("############ " + importJobURL);
    await()
        .pollDelay(500, MILLISECONDS)
        .atMost(45, MINUTES)
        .until(() -> pollForStatus(importJobURL), not(equalTo("RUNNING")));
    LOG.info("Import completed");
    return pollForStatus(importJobURL);
  }

  private static String pollForStatus(String importJobURL) {
    return given()
        .get(importJobURL)
        .then()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .path("status")
        .toString();
  }

  private static void deleteData() {
    given().delete("/api/data/sys_md_Package/v3meta").then().statusCode(NO_CONTENT.value());
  }
}
