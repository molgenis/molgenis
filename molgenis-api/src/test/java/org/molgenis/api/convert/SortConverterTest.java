package org.molgenis.api.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.api.model.Sort.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortConverterTest {
  private SortConverter sortConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    sortConverter = new SortConverter();
  }

  @Test
  void testConvert() {
    assertEquals(create("item"), sortConverter.convert("item"));
  }

  @Test
  void testConvertParseException() {
    assertThrows(SortParseException.class, () -> sortConverter.convert("-"));
  }
}
