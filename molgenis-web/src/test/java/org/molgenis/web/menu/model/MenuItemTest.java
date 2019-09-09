package org.molgenis.web.menu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MenuItemTest {

  @Test
  void itemWithParams() {
    MenuItem menuItem = MenuItem.create("id", "label", "params");
    assertEquals(menuItem.getParams(), "params");
    assertEquals(menuItem.getUrl(), "id?params");
  }

  @Test
  void itemWithOutParams() {
    MenuItem menuItem = MenuItem.create("id", "label");
    assertNull(menuItem.getParams());
    assertEquals(menuItem.getUrl(), "id");
  }
}
