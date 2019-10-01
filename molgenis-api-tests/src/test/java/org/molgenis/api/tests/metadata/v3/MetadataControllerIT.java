package org.molgenis.api.tests.metadata.v3;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

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

  @Test
  @Order(1)
  void testRetrieveMetadata() {

    // GET /api/metadata
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(2)
  void testRetrieveMetadataEntityType() {
    // GET /api/metadata/v3meta_MyDataset
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(3)
  void testRetrieveMetadataEntityTypeUnknown() {
    // GET /api/metadata/v3meta_MyUnknownDataset
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(4)
  void testRetrieveMetadataEntityTypeAttributes() {
    // GET /api/metadata/v3meta_MyDataset/attributes
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(5)
  void testRetrieveMetadataEntityTypeAttribute() {
    // GET /api/metadata/v3meta_MyDataset/attribute/myString
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(6)
  void testRetrieveMetadataEntityTypeAttributeUnknown() {
    // GET /api/metadata/v3meta_MyDataset/attribute/myUnknownAttribute
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(7)
  void testUpdateMetadataEntityType() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(8)
  void testPartialUpdateMetadataEntityType() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(9)
  void testCreateMetadataEntityTypeAttribute() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(10)
  void testUpdateMetadataEntityTypeAttribute() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(11)
  void testPartialUpdateMetadataEntityTypeAttribute() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(12)
  void testDeleteMetadataEntityTypeAttributes() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(13)
  void testDeleteMetadataEntityTypeAttribute() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(14)
  void testDeleteMetadataEntityTypes() {
    throw new UnsupportedOperationException(); // FIXME implement
  }

  @Test
  @Order(15)
  void testDeleteMetadataEntityType() {
    throw new UnsupportedOperationException(); // FIXME implement
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
