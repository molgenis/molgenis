package org.molgenis.api.convert;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.api.model.Selection.FULL_SELECTION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Selection;

class SelectionConverterTest {
  private SelectionConverter selectionConverter;

  @BeforeEach
  void setUpBeforeMethod() {
    selectionConverter = new SelectionConverter();
  }

  @Test
  void testConvert() {
    assertEquals(new Selection(singletonMap("item", null)), selectionConverter.convert("item"));
  }

  @Test
  void testConvertEmptySelection() {
    assertEquals(FULL_SELECTION, selectionConverter.convert(""));
  }

  @Test
  void testConvertParseException() {
    assertThrows(SelectionParseException.class, () -> selectionConverter.convert("item,"));
  }
}
