package org.molgenis.api.tests.search;

import static java.util.Collections.singletonList;
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
import static org.molgenis.util.ResourceUtils.getFile;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.api.tests.AbstractApiTests;
import org.slf4j.Logger;

@TestMethodOrder(OrderAnnotation.class)
class SearchAPIIT extends AbstractApiTests {
  private static final Logger LOG = getLogger(SearchAPIIT.class);
  private static String adminToken;

  @BeforeAll
  protected static void setUpBeforeClass() {
    AbstractApiTests.setUpBeforeClass();

    adminToken = AbstractApiTests.getAdminToken();

    String importStatus =
        uploadEmxFile(adminToken, getFile(SearchAPIIT.class, "search-cases.xlsx"));
    assertEquals("FINISHED", importStatus);

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
    var response =
        given()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .contentType(APPLICATION_JSON)
            .get(
                "api/data/search_mutations?q={attribute}{operator}'{value}'",
                attribute,
                operator,
                value)
            .then()
            .statusCode(OKE);
    if (contains.size() > 0) {
      var ids = response.extract().jsonPath().getList("items.data.ID").stream().collect(toSet());
      assertThat(id, ids, hasItems(contains.toArray()));
    }
    if (count != null) {
      assertEquals(count, response.extract().path("page.totalElements"), id);
    }
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
    var response =
        given()
            .header(X_MOLGENIS_TOKEN, adminToken)
            .contentType(APPLICATION_JSON)
            .get(
                "api/data/search_disease_types?q={attribute}{operator}'{value}'",
                attribute,
                operator,
                value)
            .then()
            .statusCode(OKE);
    if (contains.size() > 0) {
      var ids = response.extract().jsonPath().getList("items.data.id").stream().collect(toSet());
      assertThat(id, ids, hasItems(contains.toArray()));
    }
    if (count != null) {
      assertEquals(count, response.extract().path("page.totalElements"), id);
    }
  }
}
