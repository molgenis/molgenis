package org.molgenis.data.plugin.model;

import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class PluginPermissionDeniedExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security", "data-plugin");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new PluginPermissionDeniedException("pluginId", VIEW_PLUGIN), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "No 'View plugin' permission on plugin with id 'pluginId'."};
    return new Object[][] {enParams};
  }
}
