package org.molgenis.web.menu.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

public class MenuItemTest {

  @Test
  public void itemWithParams() {
    MenuItem menuItem = MenuItem.create("id", "label", "params");
    assertEquals(menuItem.getParams(), "params");
    assertEquals(menuItem.getUrl(), "id?params");
  }

  @Test
  public void itemWithOutParams() {
    MenuItem menuItem = MenuItem.create("id", "label");
    assertNull(menuItem.getParams());
    assertEquals(menuItem.getUrl(), "id");
  }
}
