package org.molgenis.api.tests.data.v3;

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
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.io.IOException;
import java.time.LocalDate;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.molgenis.api.tests.AbstractApiTest;
import org.molgenis.test.TestResourceUtils;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EntityControllerIT extends AbstractApiTest {
  private static final Logger LOG = getLogger(EntityControllerIT.class);

  @BeforeClass
  public static void setUpBeforeClass() {
    AbstractApiTest.setUpBeforeClass();

    importData();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    deleteData();

    AbstractApiTest.tearDownAfterClass();
  }

  @Test
  public void testCreateResource() {
    JSONArray jsonArray = new JSONArray();
    jsonArray.add("str1");
    jsonArray.add("str2");

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", 25);
    jsonObject.put("label", "Row 25");
    jsonObject.put("myBool", true);
    jsonObject.put("myDate", "2000-01-25");
    jsonObject.put("myDateTime", "2000-01-24T21:02:03Z");
    jsonObject.put("myDecimal", 25.1);
    jsonObject.put("myInt", 25);
    jsonObject.put("myLong", 3000000025L);
    jsonObject.put("myString", "String 25");
    jsonObject.put("myText", "Text 25");
    jsonObject.put("myXref", 5);
    jsonObject.put("myMref", jsonArray);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(jsonObject.toJSONString())
        .post("/api/data/v3_MyDataset")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/data/v3_MyDataset/25");
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResource() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(), "retrieveResource.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/data/v3_MyDataset/25")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResourceSubResource() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveSubResourceCollection.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/data/v3_MyDataset/1/myMref?q=id=in=(str1,str2)")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResourceCollection() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveResourceCollection.json",
            ImmutableMap.of(
                "baseUri", RestAssured.baseURI, "autoDate", LocalDate.now().toString()));

    given()
        .get("/api/data/v3_MyDataset")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResourceCollectionFilterExpand() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveResourceCollectionFilterExpand.json",
            ImmutableMap.of(
                "baseUri", RestAssured.baseURI, "autoDate", LocalDate.now().toString()));

    given()
        .get("/api/data/v3_MyDataset?filter=id,label,myXref,myMref(id)&expand=myMref&size=5&page=1")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResourceCollectionSortQuery() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveResourceCollectionSortQuery.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/data/v3_MyDataset?sort=-label&q=id=in=(1,2,3,4,5)")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResourceCollectionInvalidInput() throws IOException {
    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "retrieveResourceCollectionInvalidInput.json",
            ImmutableMap.of(
                "baseUri", RestAssured.baseURI, "autoDate", LocalDate.now().toString()));

    given()
        .get(
            "/api/data/v3_MyDataset?q=id=invalid=1&filter=id,label,myXref,myMref(id))&expand=myMref((id)&sort=--label&page=-1&size=50000")
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.BAD_REQUEST.value())
        .body(isEqualJson(expectedJson, JSONCompareMode.LENIENT));
  }

  @Test(
      dependsOnMethods = {
        "testRetrieveResource",
        "testRetrieveResourceSubResource",
        "testRetrieveResourceCollection",
        "testRetrieveResourceCollectionFilterExpand",
        "testRetrieveResourceCollectionSortQuery",
        "testRetrieveResourceCollectionInvalidInput"
      })
  public void testUpdateResource() throws IOException {
    JSONArray jsonArray = new JSONArray();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("id", 25);
    jsonObject.put("label", "Row 25b");
    jsonObject.put("myBool", false);
    jsonObject.put("myDate", "2000-02-25");
    jsonObject.put("myDateTime", "2000-02-24T21:02:03Z");
    jsonObject.put("myDecimal", 250.1);
    jsonObject.put("myInt", 250);
    jsonObject.put("myLong", 3000000250L);
    jsonObject.put("myString", "String 25b");
    jsonObject.put("myText", "Text 25b");
    jsonObject.put("myXref", null);
    jsonObject.put("myMref", jsonArray);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(jsonObject.toJSONString())
        .put("/api/data/v3_MyDataset/25")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(), "updateResource.json", ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/data/v3_MyDataset/25")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "testUpdateResource")
  public void testPartialUpdateResource() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("myString", "String 25 - updated partially");

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(jsonObject.toJSONString())
        .patch("/api/data/v3_MyDataset/25")
        .then()
        .statusCode(NO_CONTENT.value());
    given()
        .get("/api/data/v3_MyDataset/25")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("data.myString", Matchers.equalTo("String 25 - updated partially"));
  }

  @Test(dependsOnMethods = "testPartialUpdateResource")
  public void deleteResource() {
    given().delete("/api/data/v3_MyDataset/25").then().statusCode(NO_CONTENT.value());
    given().get("/api/data/v3_MyDataset/25").then().statusCode(NOT_FOUND.value());
  }

  @Test(dependsOnMethods = "deleteResourceCollectionQuery")
  public void deleteResourceCollection() throws IOException {
    given().delete("/api/data/v3_MyDataset").then().statusCode(NO_CONTENT.value());

    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "deleteResourceCollection.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/data/v3_MyDataset")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test(dependsOnMethods = "deleteResource")
  public void deleteResourceCollectionQuery() throws IOException {
    given()
        .delete("/api/data/v3_MyDataset?q=label=in=('Row%201','Row%202','Row%203','Row%204')")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "deleteResourceCollectionQuery.json",
            ImmutableMap.of(
                "baseUri", RestAssured.baseURI, "autoDate", LocalDate.now().toString()));

    given()
        .get("/api/data/v3_MyDataset")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  private static void importData() {
    String importJobStatusUrl =
        given()
            .multiPart(getFile(EntityControllerIT.class, "data_api_v3_emx.xlsx"))
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
    given().delete("/api/data/sys_md_Package/v3").then().statusCode(NO_CONTENT.value());
  }
}
