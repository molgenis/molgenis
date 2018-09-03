package org.molgenis.data.plugin.model;

import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test
public class PluginPermissionDeniedExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security", "data-plugin");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new PluginPermissionDeniedException("pluginId", VIEW_PLUGIN), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "No 'View plugin' permission on plugin with id 'pluginId'."};
    return new Object[][] {enParams};
  }
}
