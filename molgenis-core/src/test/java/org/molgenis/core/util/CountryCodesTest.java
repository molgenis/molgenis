package org.molgenis.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CountryCodesTest {
  @Test
  void testGetString() {
    assertEquals(CountryCodes.get("NL"), "Netherlands");
  }

  @Test
  void testGet() {
    assertEquals(CountryCodes.get().size(), 250);
  }
}
