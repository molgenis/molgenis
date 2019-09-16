package org.molgenis.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PluginControllerTest {

  @Test
  void MolgenisPlugin() {
    assertThrows(
        IllegalArgumentException.class, () -> new PluginController("/invalidprefix/test") {});
  }

  @Test
  void getId() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = new PluginController(uri) {};
    assertEquals("test", molgenisPlugin.getId());
  }

  @Test
  void getUri() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = new PluginController(uri) {};
    assertEquals(uri, molgenisPlugin.getUri());
  }
}
