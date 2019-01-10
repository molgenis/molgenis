package org.molgenis.web.menu;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class MenuUtilsTest {

  @Test
  public void getMenuJson() {
    assertNotNull(MenuUtils.readDefaultMenuValueFromClasspath());
  }
}
