package org.molgenis.web;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class PluginControllerTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MolgenisPlugin() {
    new PluginController("/invalidprefix/test") {};
  }

  @Test
  public void getId() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = new PluginController(uri) {};
    assertEquals(molgenisPlugin.getId(), "test");
  }

  @Test
  public void getUri() {
    String uri = PluginController.PLUGIN_URI_PREFIX + "test";
    PluginController molgenisPlugin = new PluginController(uri) {};
    assertEquals(molgenisPlugin.getUri(), uri);
  }
}
