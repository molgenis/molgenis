package org.molgenis.web.menu;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MenuUtilsTest {

  @Test
  void getMenuJson() {
    assertNotNull(MenuUtils.readDefaultMenuValueFromClasspath());
  }
}
