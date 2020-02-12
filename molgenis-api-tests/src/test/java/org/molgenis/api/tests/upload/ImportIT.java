package org.molgenis.api.tests.upload;

import static org.molgenis.test.IsEqualJson.isEqualJson;
import static org.molgenis.util.ResourceUtils.getFile;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

import com.google.common.collect.ImmutableMap;
import io.restassured.RestAssured;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTests;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.molgenis.test.TestResourceUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

@TestMethodOrder(OrderAnnotation.class)
class ImportIT extends AbstractApiTests {
  private static final Logger LOG = getLogger(ImportIT.class);
  private static String adminToken;

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTests.setUpBeforeClass();

    adminToken = AbstractApiTests.getAdminToken();
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    AbstractApiTests.tearDownAfterClass();
  }

  @Test
  void testImportIdFile() throws IOException {
    RestTestUtils.uploadEmxFile(adminToken, getFile(ImportIT.class, "123-test-group.xlsx"));

    String expectedMetaJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "123-test-groep_ID-TestMeta.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));

    given()
        .get("/api/metadata?q=id==123-test-group_ID-Test")
        .then()
        .statusCode(OK.value())
        .body(isEqualJson(expectedMetaJson));

    String expectedDataJson =
        TestResourceUtils.getRenderedString(
            getClass(),
            "123-test-groep_ID-Test-RefData.json",
            ImmutableMap.of("baseUri", RestAssured.baseURI));
    given()
        .get("/api/data/123-test-group_ID-Test-Ref/ref5")
        .then()
        .statusCode(HttpStatus.OK.value())
        .body(isEqualJson(expectedDataJson));

    given()
        .delete("/api/metadata/123-test-group_ID-Test")
        .then()
        .statusCode(ACCEPTED.value())
        .extract()
        .header(LOCATION);
    given()
        .delete("/api/metadata/123-test-group_ID-Test-Ref")
        .then()
        .statusCode(ACCEPTED.value())
        .extract()
        .header(LOCATION);
  }
}
