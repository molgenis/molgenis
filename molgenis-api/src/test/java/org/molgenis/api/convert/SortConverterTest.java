package org.molgenis.api.convert;

import org.molgenis.api.model.Sort;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SortConverterTest {
  private SortConverter sortConverter;

  @BeforeMethod
  public void setUpBeforeMethod() {
    sortConverter = new SortConverter();
  }

  @Test
  public void testConvert() {
    Assert.assertEquals(sortConverter.convert("item"), Sort.create("item"));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testConvertParseException() {
    sortConverter.convert("+");
  }
}
