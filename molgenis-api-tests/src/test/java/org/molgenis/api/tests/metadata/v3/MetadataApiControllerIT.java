package org.molgenis.api.tests.metadata.v3;

import static org.molgenis.test.IsEqualJson.isEqualJson;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.molgenis.api.tests.AbstractApiTest;
import org.molgenis.test.TestResourceUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

@TestMethodOrder(OrderAnnotation.class)
class MetadataApiControllerIT extends AbstractApiTest {
  private static final Logger LOG = getLogger(MetadataApiControllerIT.class);

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTest.setUpBeforeClass();

    createPackage();
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    deletePackage();

    AbstractApiTest.tearDownAfterClass();
  }

  @ParameterizedTest
  @ValueSource(strings = {"Identifiable", "Describable", "MyNumbers", "MyStrings", "MyDataset"})
  @Order(1)
  void testCreateMetadataEntityTypeIdentifiable(String datasetName) throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "createMetadataEntityType" + datasetName + ".json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .post("/api/metadata")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/metadata/v3meta_" + datasetName);
  }

  @Test
  @Order(2)
  void testRetrieveMetadata() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(), "retrieveMetadata.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata?q=package==v3meta")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(3)
  void testRetrieveMetadataEntityType() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyNumbers")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(4)
  void testRetrieveMetadataEntityTypeAttributes() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityTypeAttributes.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyDataset/attributes?size=3&page=2")
        .then()
          .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(5)
  void testRetrieveMetadataEntityTypeAttribute() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataEntityTypeAttribute.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyDataset/attributes/v3meta_MyDataset_myString")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(6)
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
  @Order(7)
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
  @Order(8)
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
  @Order(9)
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
  @Order(10)
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
  @Order(11)
  void testDeleteMetadataEntityTypeAttributes() {
    given()
        .delete("/api/metadata/v3meta_MyDataset/attributes?q=id=in=(myDate,myDateTime)")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(12)
  void testDeleteMetadataEntityTypeAttribute() {
    given()
        .delete("/api/metadata/v3meta_MyDataset/attributes/myString")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(13)
  void testDeleteMetadataEntityType() {
    given().delete("/api/metadata/v3meta_MyDataset").then().statusCode(NO_CONTENT.value());
  }

  @Test
  @Order(14)
  void testDeleteMetadataEntityTypes() {
    given()
        .delete("/api/metadata?q=id=in=(MyNumbers,MyStrings)")
        .then()
        .statusCode(NO_CONTENT.value());
  }

  private static void createPackage() {
    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body("{\"id\":\"v3meta\",\"label\":\"v3meta\"}")
        .post("/api/data/sys_md_Package")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/data/sys_md_Package/v3meta");
  }

  private static void deletePackage() {
    given().delete("/api/data/sys_md_Package/v3meta").then().statusCode(NO_CONTENT.value());
  }
}
