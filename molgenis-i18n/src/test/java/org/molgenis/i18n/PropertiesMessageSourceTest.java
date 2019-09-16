package org.molgenis.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.util.i18n.PropertiesMessageSource;

class PropertiesMessageSourceTest {
  PropertiesMessageSource propertiesMessageSource = new PropertiesMessageSource(" naMespace\t");

  @Test
  void testGetNamespace() {
    assertEquals("namespace", propertiesMessageSource.getNamespace());
  }
}
