package org.molgenis.web.menu.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class MenuItemTest {

  @Test
  void itemWithParams() {
    MenuItem menuItem = MenuItem.create("id", "label", "params");
    assertEquals("params", menuItem.getParams());
    assertEquals("id/params", menuItem.getUrl());
  }

  @Test
  void itemWithOutParams() {
    MenuItem menuItem = MenuItem.create("id", "label");
    assertNull(menuItem.getParams());
    assertEquals("id", menuItem.getUrl());
  }
}
