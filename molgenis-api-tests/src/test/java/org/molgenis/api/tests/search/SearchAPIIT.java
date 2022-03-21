package org.molgenis.api.tests.search;

import static freemarker.template.utility.Collections12.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.api.tests.utils.RestTestUtils.APPLICATION_JSON;
import static org.molgenis.api.tests.utils.RestTestUtils.OKE;
import static org.molgenis.api.tests.utils.RestTestUtils.X_MOLGENIS_TOKEN;
import static org.molgenis.api.tests.utils.RestTestUtils.removePackages;
import static org.molgenis.api.tests.utils.RestTestUtils.uploadEmxFile;
import static org.molgenis.api.tests.utils.RestTestUtils.waitForIndexJobs;

import io.restassured.response.ValidatableResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.api.tests.AbstractApiTests;

@TestMethodOrder(OrderAnnotation.class)
class SearchAPIIT extends AbstractApiTests {
  private static String adminToken;

  private static File createEMX() {
    try {
      File file = File.createTempFile("search", "csv.zip");
      var outputstream = new ZipOutputStream(new FileOutputStream(file));
      var files =
          List.of(
              "attributes.tsv",
              "entities.tsv",
              "packages.tsv",
              "tags.tsv",
              "search_disease_types.tsv",
              "search_disease_types-cases.tsv",
              "search_mutations.tsv",
              "search_mutations-cases.tsv",
              "search_servers.tsv",
              "search_servers-cases.tsv");
      for (String filename : files) {
        outputstream.putNextEntry(new ZipEntry(filename));
        IOUtils.copy(SearchAPIIT.class.getResourceAsStream(filename), outputstream);
        outputstream.closeEntry();
      }
      outputstream.close();
      return file;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTests.setUpBeforeClass();

    adminToken = AbstractApiTests.getAdminToken();

    var emxFile = createEMX();
    String importStatus = uploadEmxFile(adminToken, emxFile);
    assertEquals("FINISHED", importStatus);
    emxFile.delete();

    waitForIndexJobs(adminToken);
  }

  @AfterAll
  protected static void tearDownAfterClass() {
    removePackages(adminToken, singletonList("search"));
    AbstractApiTests.tearDownAfterClass();
  }

  private static String getRsqlOperator(String name) {
    switch (name) {
      case "SEARCH":
        return "=q=";
      case "LIKE":
        return "=like=";
      case "SEARCH_QUERY":
        return "=sq=";
      default:
        throw new IllegalArgumentException(name);
    }
  }

  private static Arguments getTestCaseArguments(Map testcase, String idAttributeName) {
    List<Map<String, Object>> expected = (List<Map<String, Object>>) testcase.get("contains");
    return Arguments.of(
        testcase.get("id"),
        testcase.get("attribute"),
        getRsqlOperator((String) testcase.get("operator")),
        testcase.get("value"),
        testcase.get("count"),
        expected.stream().map(it -> it.get(idAttributeName)).collect(toList()),
        testcase.get("comment"));
  }

  private static Stream<Arguments> getCases(String cases) {
    var response =
        given()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .contentType(APPLICATION_JSON)
            .get("api/v2/{cases}", cases)
            .then()
            .statusCode(OKE);
    String idAttribute = response.extract().path("meta.attributes[5].refEntity.idAttribute");
    var items = response.extract().jsonPath().getList("items");
    return items.stream()
        .map(Map.class::cast)
        .map(testcase -> getTestCaseArguments(testcase, idAttribute));
  }

  private static Stream<Arguments> provideMutationsCases() {
    return getCases("search_mutations-cases");
  }

  @ParameterizedTest()
  @MethodSource("provideMutationsCases")
  void testSearchMutations(
      String id,
      String attribute,
      String operator,
      String value,
      Integer count,
      List<String> contains,
      String comment) {
    ValidatableResponse response = doQuery("search_mutations", attribute, operator, value);
    if (contains.size() > 0) {
      var ids = response.extract().jsonPath().getList("items.data.ID").stream().collect(toSet());
      assertThat(id, ids, hasItems(contains.toArray()));
    }
    if (count != null) {
      assertEquals(count, response.extract().path("page.totalElements"), id);
    }
  }

  private ValidatableResponse doQuery(
      String entity, String attribute, String operator, String value) {
    return given()
        .header(X_MOLGENIS_TOKEN, adminToken)
        .contentType(APPLICATION_JSON)
        .param("q", String.format("%s%s'%s'", attribute, operator, value))
        .get("api/data/{entity}", entity)
        .then()
        .statusCode(OKE);
  }

  private static Stream<Arguments> provideDiseaseTypesCases() {
    return getCases("search_disease_types-cases");
  }

  @ParameterizedTest()
  @MethodSource("provideDiseaseTypesCases")
  void testSearchDiseaseTypes(
      String id,
      String attribute,
      String operator,
      String value,
      Integer count,
      List<String> contains,
      String comment) {
    var response = doQuery("search_disease_types", attribute, operator, value);
    if (contains.size() > 0) {
      var ids = response.extract().jsonPath().getList("items.data.id").stream().collect(toSet());
      assertThat(id, ids, hasItems(contains.toArray()));
    }
    if (count != null) {
      assertEquals(count, response.extract().path("page.totalElements"), id);
    }
  }

  private static Stream<Arguments> provideServersCases() {
    return getCases("search_servers-cases");
  }

  @ParameterizedTest()
  @MethodSource("provideServersCases")
  void testSearchServers(
      String id,
      String attribute,
      String operator,
      String value,
      Integer count,
      List<String> contains,
      String comment) {
    var response = doQuery("search_servers", attribute, operator, value);
    if (contains.size() > 0) {
      var ids = response.extract().jsonPath().getList("items.data.id").stream().collect(toSet());
      assertThat(id, ids, hasItems(contains.toArray()));
    }
    if (count != null) {
      assertEquals(count, response.extract().path("page.totalElements"), id);
    }
  }
}
