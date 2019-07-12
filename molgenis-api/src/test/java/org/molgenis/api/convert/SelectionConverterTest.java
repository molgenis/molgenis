package org.molgenis.api.convert;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import org.molgenis.api.model.Selection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SelectionConverterTest {
  private SelectionConverter selectionConverter;

  @BeforeMethod
  public void setUpBeforeMethod() {
    selectionConverter = new SelectionConverter();
  }

  @Test
  public void testConvert() {
    assertEquals(
        selectionConverter.convert("item"), new Selection(Collections.singletonMap("item", null)));
  }

  @Test
  public void testConvertEmptySelection() {
    assertEquals(selectionConverter.convert(""), Selection.FULL_SELECTION);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testConvertParseException() {
    selectionConverter.convert("item,");
  }
}
