package org.molgenis.core.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class CountryCodesTest {
  @Test
  public void testGetString() {
    assertEquals(CountryCodes.get("NL"), "Netherlands");
  }

  @Test
  public void testGet() {
    assertEquals(CountryCodes.get().size(), 250);
  }
}
