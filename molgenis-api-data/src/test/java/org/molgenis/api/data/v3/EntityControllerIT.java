package org.molgenis.api.data.v3;

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

import io.restassured.RestAssured;
import java.io.IOException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.hamcrest.Matchers;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

// TODO decide which test cases to move to controller unit test class
// 0. check api tests of v1 and v2 and include relevant test cases
// 1. all data types (except html/hyperlink etc.)
// 2. null values
// 3. computed (for update/create)
// 4. mappedBy (for update/create)
// 5. compounds
// 6. abstract: abstract entity types + concrete entity types that extend from abstract entity type
// 7. transactions
// 8. exceptions (including security)
// 9. different identifier data types (also for references)
// 10. request zero-items case
// 11. error codes
// 12. encoding entity type identifiers, entity identifiers etc.
// 13 de REST hack
public class EntityControllerIT extends AbstractApiTest {
  private static final Logger LOG = getLogger(EntityControllerIT.class);

  @BeforeClass
  public static void setUpBeforeClass() {
    AbstractApiTest.setUpBeforeClass();

    importData();
  }

  @AfterClass
  public static void tearDownAfterClass() {
    AbstractApiTest.tearDownAfterClass();

    // deleteData();
  }

  // 1. location header
  // exc: already exists
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
        .post("/api/entity/v3_MyDataset")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/entity/v3_MyDataset/25");
  }

  // 1. filter (basic? multiple? nested?)
  // 2. expand
  // 3. query
  // 4. sort
  // exc: entity type does not exist
  // exc: entity does not exist
  // exc: no permission to read entity type
  @Test(dependsOnMethods = "testCreateResource")
  public void testRetrieveResource() throws IOException {
    String expectedJson = ResourceUtils.getString(getClass(), "retrieveResource.json");

    given()
        .get("/api/entity/v3_MyDataset/25")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedJson));
  }

  @Test
  public void testRetrieveResourceSubResource() {
    // exc: entity type exists
    // exc: entity exists
    // exc: attribute does not exist
    throw new UnsupportedOperationException();
  }

  @Test
  public void testRetrieveResourceCollection() {
    // 1. paging
    // 2. links: first page and last page
    // exc: max page size exceeded
    throw new UnsupportedOperationException();
  }

  @Test(dependsOnMethods = "testRetrieveResource")
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
        .put("/api/entity/v3_MyDataset/25")
        .then()
        .statusCode(NO_CONTENT.value());

    String expectedJson = ResourceUtils.getString(getClass(), "updateResource.json");

    given()
        .get("/api/entity/v3_MyDataset/25")
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
        .patch("/api/entity/v3_MyDataset/25")
        .then()
        .statusCode(NO_CONTENT.value());
    given()
        .get("/api/entity/v3_MyDataset/25")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body("data.myString", Matchers.equalTo("String 25 - updated partially"));
  }

  @Test(dependsOnMethods = "testPartialUpdateResource")
  public void deleteResource() {
    given().delete("/api/entity/v3_MyDataset/25").then().statusCode(NO_CONTENT.value());
    given().get("/api/entity/v3_MyDataset/25").then().statusCode(NOT_FOUND.value());
  }

  // TODO implement
  @Test
  public void deleteResourceCollection() {
    throw new UnsupportedOperationException();
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
}
