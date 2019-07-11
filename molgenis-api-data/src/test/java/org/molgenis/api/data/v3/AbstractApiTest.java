package org.molgenis.api.data.v3;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractApiTest {

  public static final String API_TEST_HOST = "API_TEST_HOST";
  private static String ADMIN_TOKEN;

  /**
   * Subclasses most call this method:
   *
   * <pre>
   * &#64;BeforeClass
   * public static void setUpBeforeClass() {
   *   AbstractApiTest.setUpBeforeClass();
   *   ...
   * }
   * </pre>
   */
  protected static void setUpBeforeClass() {
    String restTestHost = getSystemProperty(API_TEST_HOST);
    RestAssured.baseURI = restTestHost;
    String restTestAdminName = getSystemProperty("API_TEST_ADMIN_NAME");
    String restTestAdminPw = getSystemProperty("API_TEST_ADMIN_PW");

    ADMIN_TOKEN = login(restTestAdminName, restTestAdminPw);
  }

  protected static Configuration getFreemarkerConfig(File templatePath) {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
    try {
      cfg.setDirectoryForTemplateLoading(templatePath);
      cfg.setDefaultEncoding("UTF-8");
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setLogTemplateExceptions(false);
      cfg.setWrapUncheckedExceptions(true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return cfg;
  }

  protected static String getProcessedTemplate(String templateName, Configuration configuration) {
    return getProcessedTemplate(templateName, configuration, Collections.emptyMap());
  }

  protected static String getProcessedTemplate(
      String templateName, Configuration configuration, Map<String, Object> additionalValues) {
    Writer out = new StringWriter();
    try {
      Template template = configuration.getTemplate(templateName);
      Map<String, Object> values = new HashMap<>();
      String restTestHost = getSystemProperty(API_TEST_HOST);
      values.put(API_TEST_HOST, restTestHost);
      values.putAll(additionalValues);
      template.process(values, out);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return out.toString();
  }

  private static String getSystemProperty(String propertyName) {
    String propertyValue = System.getProperty(propertyName);
    if (propertyValue == null) {
      throw new IllegalArgumentException("\"System property '" + propertyName + "' undefined");
    }
    return propertyValue;
  }

  /**
   * Subclasses most call this method:
   *
   * <pre>
   * &#64;AfterClass
   * public static void tearDownAfterClass() {
   *   AbstractApiTest.tearDownAfterClass();
   *   ...
   * }
   * </pre>
   */
  protected static void tearDownAfterClass() {
    logout();
  }

  protected static RequestSpecification given() {
    return RestAssured.given()
        .header("x-molgenis-token", ADMIN_TOKEN)
        .accept(APPLICATION_JSON_VALUE)
        .log()
        .ifValidationFails();
  }

  private static String login(String username, String password) {
    return RestAssured.given()
        .contentType(APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON_VALUE)
        .body(format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password))
        .log()
        .ifValidationFails()
        .post("/api/v1/login")
        .then()
        .statusCode(OK.value())
        .extract()
        .path("token");
  }

  private static void logout() {
    given().post("/api/v1/logout").then().statusCode(OK.value());
  }
}
