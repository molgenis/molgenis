package org.molgenis.core.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPluginException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.Ui;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisMenuControllerTest extends AbstractMockitoTest {
  private MolgenisMenuController molgenisMenuController;

  @Mock private DataService dataService;

  @BeforeMethod
  public void beforeMethod() {
    molgenisMenuController = new MolgenisMenuController(mock(Ui.class), "1", "1", dataService);
  }

  @Test(
      expectedExceptions = UnknownPluginException.class,
      expectedExceptionsMessageRegExp = "id:pluginA")
  public void testGetForwardPluginUriUnknownPlugin() {
    String pluginId = "pluginA";
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(null);
    molgenisMenuController.getForwardPluginUri(pluginId, null);
  }

  @Test
  public void testGetForwardPluginUri() {
    String pluginId = "pluginB";
    Plugin plugin = mock(Plugin.class);
    when(plugin.getPath()).thenReturn("app/test");
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);

    String uri = molgenisMenuController.getForwardPluginUri(pluginId, "");

    assertEquals(uri, "forward:/plugin/app/test/");
  }
}
