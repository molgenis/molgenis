package org.molgenis.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.core.util.CountryCodes.get;

import org.junit.jupiter.api.Test;

class CountryCodesTest {
  @Test
  void testGetString() {
    assertEquals("Netherlands", get("NL"));
  }

  @Test
  void testGet() {
    assertEquals(250, get().size());
  }
}
