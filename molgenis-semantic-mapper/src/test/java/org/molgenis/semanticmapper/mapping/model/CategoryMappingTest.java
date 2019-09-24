package org.molgenis.semanticmapper.mapping.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.semanticmapper.mapping.model.CategoryMapping.create;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

class CategoryMappingTest {
  private CategoryMapping<String, String> mapping =
      CategoryMapping.create("blah", ImmutableMap.of("Human", "1", "Orc", "2"));

  private CategoryMapping<String, String> mappingWithNullValueInMap =
      CategoryMapping.create("$('LifeLines_GENDER').map({\"0\":null,\"1\":\"0\"}).value();");

  private CategoryMapping<String, String> mappingWithDefault =
      CategoryMapping.create("blah", ImmutableMap.of("Human", "1", "Orc", "2"), "3");

  private CategoryMapping<String, String> mappingWithDefaultNull =
      CategoryMapping.create("blah", ImmutableMap.of("Human", "1", "Orc", "2"), null);

  private CategoryMapping<String, String> mappingWithNullValue =
      CategoryMapping.create("blah", ImmutableMap.of("Human", "1", "Orc", "2"), "3", "5");

  private CategoryMapping<String, String> mappingWithNullValueEqualsNull =
      CategoryMapping.create("blah", ImmutableMap.of("Human", "1", "Orc", "2"), "3", null);

  @Test
  void testCreateFromAlgorithm() {
    assertEquals(mapping, create(mapping.getAlgorithm()));
    assertEquals(mappingWithNullValueInMap, create(mappingWithNullValueInMap.getAlgorithm()));
    assertEquals(mappingWithDefault, create(mappingWithDefault.getAlgorithm()));
    assertEquals(mappingWithDefaultNull, create(mappingWithDefaultNull.getAlgorithm()));
    assertEquals(mappingWithNullValue, create(mappingWithNullValue.getAlgorithm()));
    assertEquals(
        mappingWithNullValueEqualsNull, create(mappingWithNullValueEqualsNull.getAlgorithm()));
  }

  @Test
  void testGetAlgorithm() {
    assertEquals("$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}).value();", mapping.getAlgorithm());

    assertEquals(
        "$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\").value();",
        mappingWithDefault.getAlgorithm());

    assertEquals(
        "$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\", \"5\").value();",
        mappingWithNullValue.getAlgorithm());

    assertEquals(
        "$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\", null).value();",
        mappingWithNullValueEqualsNull.getAlgorithm());
  }
}
