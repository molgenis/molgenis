package org.molgenis.oneclickimporter.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.oneclickimporter.service.impl.AttributeTypeServiceImpl;

class AttributeTypeServiceTest {
  AttributeTypeService attributeTypeService = new AttributeTypeServiceImpl();

  @Test
  void guessBasicTypes() {
    List<Object> columnValues = newArrayList(1, 2, 3);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList("a", "b", "c");
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(true, false, true);
    assertEquals(BOOL, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1.1, 1.2, 1.3);
    assertEquals(DECIMAL, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1L, 2L, 3L);
    assertEquals(LONG, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1L, "abc", 3L);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));
  }

  @Test
  void guessTypesWithNullValues() {
    List<Object> columnValues = newArrayList(null, null, null);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1, null, null);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(null, 2, null);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(null, null, 3);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));
  }

  @Test
  void guessTypesWithMixedValues() {
    List<Object> columnValues = newArrayList(1, "2", null);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(53, "Hallo", 2);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = singletonList(null);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues =
        newArrayList(
            53,
            "This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. ",
            "This is a short string",
            "String...",
            34.1);
    assertEquals(TEXT, attributeTypeService.guessAttributeType(columnValues));
  }

  @Test
  void guessEnrichedTypes() {
    List<Object> columnValues =
        newArrayList(
            "This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. ",
            "This is a short string",
            "String...");
    assertEquals(TEXT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList("2018-01-03T00:00", "2010-05-03T00:00", "2018-02-03T00:00");
    assertEquals(DATE, attributeTypeService.guessAttributeType(columnValues));

    columnValues =
        newArrayList("2018-01-03T00:00", "2010-05-03T00:00", "2018-02-03T00:00", "Hello World!");
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1.0d);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1.1d);
    assertEquals(DECIMAL, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(Integer.MAX_VALUE + 1.5);
    assertEquals(DECIMAL, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(Integer.MAX_VALUE + 1.0);
    assertEquals(LONG, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1248723743178143923L);
    assertEquals(LONG, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1);
    assertEquals(INT, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(null, null);
    assertEquals(STRING, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(123, 1239472398547932875L);
    assertEquals(LONG, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(1, 2.2, 3);
    assertEquals(DECIMAL, attributeTypeService.guessAttributeType(columnValues));

    columnValues = newArrayList(123, 54, 1239472398547932875L, 23.0);
    assertEquals(LONG, attributeTypeService.guessAttributeType(columnValues));
  }
}
