package org.molgenis.api.tests.metadata.v3;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.molgenis.test.IsEqualJson.isEqualJson;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTest;
import org.molgenis.test.TestResourceUtils;
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

  @Test
  @Order(1)
  void testRetrieveMetadata() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(), "retrieveMetadata.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(2)
  void testRetrieveMetadataEntityType() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyDataset")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(3)
  void testRetrieveMetadataEntityTypeAttributes() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityTypeAttributes.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyDataset/attributes")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(4)
  void testRetrieveMetadataEntityTypeAttribute() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityTypeAttributes.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyDataset/attributes/myString")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(5)
  void testUpdateMetadataEntityType() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "updateMetadataEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .put("/api/metadata/v3meta_MyDataset")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(6)
  void testPartialUpdateMetadataEntityType() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "partialUpdateMetadataEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .patch("/api/metadata/v3meta_MyDataset")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(7)
  void testCreateMetadataEntityTypeAttribute() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "createMetadataEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .post("/api/metadata/v3meta_MyDataset")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/metadata/v3meta_MyDataset/fixme");
  }

  @Test
  @Order(8)
  void testUpdateMetadataEntityTypeAttribute() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "updateMetadataEntityTypeAttribute.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .put("/api/metadata/v3meta_MyDataset/attributes/myString")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(9)
  void testPartialUpdateMetadataEntityTypeAttribute() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "partialUpdateMetadataEntityTypeAttribute.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .patch("/api/metadata/v3meta_MyDataset/attributes/myString")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(10)
  void testDeleteMetadataEntityTypeAttributes() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(11)
  void testDeleteMetadataEntityTypeAttribute() {
    given()
        .delete("/api/metadata/v3meta_MyDataset/attributes/myString")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(12)
  void testDeleteMetadataEntityTypes() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(13)
  void testDeleteMetadataEntityType() {
    given().delete("/api/metadata/v3meta_MyDataset").then().statusCode(NO_CONTENT.value());
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
