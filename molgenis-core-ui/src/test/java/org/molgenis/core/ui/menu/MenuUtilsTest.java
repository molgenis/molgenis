package org.molgenis.core.ui.menu;

import static org.testng.Assert.assertNotNull;

import org.molgenis.web.menu.MenuUtils;
import org.testng.annotations.Test;

public class MenuUtilsTest {

  @Test
  public void getMenuJson() {
    assertNotNull(MenuUtils.readDefaultMenuValueFromClasspath());
  }
}
