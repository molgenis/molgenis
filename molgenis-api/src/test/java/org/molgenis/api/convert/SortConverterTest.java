package org.molgenis.api.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Sort;

class SortConverterTest {
  private SortConverter sortConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    sortConverter = new SortConverter();
  }

  @Test
  void testConvert() {
    assertEquals(sortConverter.convert("item"), Sort.create("item"));
  }

  @Test
  void testConvertParseException() {
    assertThrows(SortParseException.class, () -> sortConverter.convert("-"));
  }
}
