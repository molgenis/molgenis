package org.molgenis.data.elasticsearch.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.elasticsearch.index.query.QueryBuilder;

public class QueryBuilderAssertions {
  private QueryBuilderAssertions() {}

  public static void assertQueryBuilderEquals(QueryBuilder expected, QueryBuilder actual) {
    // QueryBuilder classes do not implement equals
    assertEquals(
        expected.toString().replaceAll("\\s", ""), actual.toString().replaceAll("\\s", ""));
  }
}
