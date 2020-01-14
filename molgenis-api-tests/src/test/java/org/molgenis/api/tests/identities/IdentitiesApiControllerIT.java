package org.molgenis.api.tests.identities;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.molgenis.api.tests.AbstractApiTest;

@TestMethodOrder(OrderAnnotation.class)
class IdentitiesApiControllerIT extends AbstractApiTest {
  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTest.setUpBeforeClass();
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    AbstractApiTest.tearDownAfterClass();
  }

  @Test
  @Order(1)
  void testCreateGroup() {
    String groupName = "My-Group";
    String bodyJson = format("{\"name\":\"%s\",\"label\":\"%s\"}", groupName, groupName);

    given()
        .contentType(APPLICATION_JSON_VALUE)
        .body(bodyJson)
        .post("/api/identities/group")
        .then()
        .statusCode(CREATED.value())
        .header(LOCATION, RestAssured.baseURI + "/api/identities/group/" + groupName);

    given().get(format("/api/identities/group/%s/role", groupName)).then().statusCode(OK.value());

    deleteGroup(groupName);
  }

  private void deleteGroup(String groupName) {
    given()
        .delete(format("/api/identities/group/%s", groupName))
        .then()
        .statusCode(NO_CONTENT.value());
  }
}
