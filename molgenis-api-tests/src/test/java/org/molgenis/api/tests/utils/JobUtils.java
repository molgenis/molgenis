package org.molgenis.api.tests.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.isOneOf;

import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpStatus;

public class JobUtils {
  private JobUtils() {}

  public static String waitJobCompletion(
      RequestSpecification requestSpecification, String location) {
    return await()
        .pollDelay(0, MILLISECONDS)
        .pollInterval(500, MILLISECONDS)
        .atMost(1, MINUTES)
        .until(
            () -> pollForStatus(requestSpecification, location),
            isOneOf("SUCCESS", "FAILED", "CANCELED"));
  }

  private static String pollForStatus(
      RequestSpecification requestSpecification, String importJobURL) {
    return requestSpecification
        .get(importJobURL)
        .then()
        .log()
        .all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .path("data.status")
        .toString();
  }
}
