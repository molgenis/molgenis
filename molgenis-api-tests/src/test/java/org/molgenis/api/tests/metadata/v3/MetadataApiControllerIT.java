package org.molgenis.api.tests.metadata.v3;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.test.IsEqualJson.isEqualJson;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.molgenis.api.tests.AbstractApiTest;
import org.molgenis.api.tests.utils.JobUtils;
import org.molgenis.test.TestResourceUtils;
import org.springframework.http.HttpStatus;

@TestMethodOrder(OrderAnnotation.class)
class MetadataApiControllerIT extends AbstractApiTest {
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
  @ValueSource(
      strings = {
        "Identifiable",
        "Describable",
        "MyNumbers",
        "MyStrings",
        "MyDataset",
        "MyOneToMany"
      })
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
    testRetrieveMetadataEntityTypeAttribute(
        "v3meta_MyDataset",
        "v3meta_MyDataset_myString",
        "retrieveMetadataEntityTypeAttribute.json");
  }

  @Test
  @Order(6)
  void testUpdateMetadataEntityType() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "updateMetadataEntityTypeMyNumbers.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    String location =
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(bodyJson)
            .put("/api/metadata/v3meta_MyNumbers")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));

    // TODO validate updated entity type
  }

  @Test
  @Order(7)
  void testPartialUpdateMetadataEntityType() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "partialUpdateMetadataEntityTypeMyStrings.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    String location =
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(bodyJson)
            .patch("/api/metadata/v3meta_MyStrings")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));

    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveMetadataPartialUpdateEntityType.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/v3meta_MyStrings")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  @Order(8)
  void testCreateMetadataEntityTypeAttribute() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "createMetadataEntityTypeAttribute.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    String location =
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(bodyJson)
            .post("/api/metadata/v3meta_MyStrings/attributes")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertAll(
        () -> assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location)),
        () ->
            testRetrieveMetadataEntityTypeAttribute(
                "v3meta_MyStrings",
                "v3meta_MyDataset_myStringNew",
                "createRetrieveMetadataEntityTypeAttribute.json"));
  }

  @Test
  @Order(9)
  void testUpdateMetadataEntityTypeAttribute() throws IOException {
    String bodyJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "updateMetadataEntityTypeAttribute.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    String location =
        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(bodyJson)
            .put("/api/metadata/v3meta_MyDataset/attributes/v3meta_MyDataset_myString")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertAll(
        () -> assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location)),
        () ->
            testRetrieveMetadataEntityTypeAttribute(
                "v3meta_MyDataset",
                "v3meta_MyDataset_myString",
                "updateRetrieveMetadataEntityTypeAttribute.json"));
  }

  // TODO enable after endpoint is async

  @Disabled
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
    String location =
        given()
            .delete(
                "/api/metadata/v3meta_MyDataset/attributes?q=id=in=(v3meta_MyDataset_myDate,v3meta_MyDataset_myDateTime)")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));
  }

  @Test
  @Order(12)
  void testDeleteMetadataEntityTypeAttribute() {
    String location =
        given()
            .delete("/api/metadata/v3meta_MyDataset/attributes/v3meta_MyDataset_myString")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));
  }

  @Test
  @Order(13)
  void testDeleteMetadataEntityType() {
    String location =
        given()
            .delete("/api/metadata/v3meta_MyDataset")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));
  }

  @Test
  @Order(14)
  void testDeleteMetadataEntityTypes() {
    String location =
        given()
            .delete("/api/metadata?q=id=in=(v3meta_MyNumbers,v3meta_MyStrings)")
            .then()
            .statusCode(ACCEPTED.value())
            .extract()
            .header(LOCATION);

    assertEquals("SUCCESS", JobUtils.waitJobCompletion(given(), location));
  }

  private void testRetrieveMetadataEntityTypeAttribute(
      String entityTypeId, String attributeId, String resourceName) throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(), resourceName, ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata/" + entityTypeId + "/attributes/" + attributeId)
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
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
