package org.molgenis.api.tests;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Strings;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.api.tests.utils.RestTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ForwardedHeaderAPIIT {

  private static final Logger LOG = LoggerFactory.getLogger(ForwardedHeaderAPIIT.class);

  /**
   * Pass down system properties via the mvn commandline argument
   *
   * <p>example: mvn test -Dtest="ForwardedHeaderAPIIT"
   * -DREST_TEST_HOST="https://latest.test.molgenis.org/"
   */
  @BeforeAll
  static void beforeClass() {
    LOG.info("Read environment variables");
    String envHost = System.getProperty("REST_TEST_HOST");
    RestAssured.baseURI = Strings.isNullOrEmpty(envHost) ? RestTestUtils.DEFAULT_HOST : envHost;
    LOG.info("baseURI: " + RestAssured.baseURI);
  }

  @Test
  void getWithForwardedProtocol() {
    Response response =
        given()
            .header("X-Forwarded-Proto", "https")
            .when()
            .get("molgenis.R")
            .then()
            .extract()
            .response();
    assertTrue(response.asString().contains("molgenis.api.url <- paste0(\"https://"));
  }
}
