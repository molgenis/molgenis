package org.molgenis.api.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
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
    assertEquals(
        selectionConverter.convert("item"), new Selection(Collections.singletonMap("item", null)));
  }

  @Test
  void testConvertEmptySelection() {
    assertEquals(selectionConverter.convert(""), Selection.FULL_SELECTION);
  }

  @Test
  void testConvertParseException() {
    assertThrows(SelectionParseException.class, () -> selectionConverter.convert("item,"));
  }
}
