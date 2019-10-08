package org.molgenis.core.ui.style;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.settings.SettingsPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      StyleSheetMetadata.class,
      StyleSheetFactory.class,
      FileMetaMetadata.class,
      SettingsPackage.class
    })
public class StyleSheetTest extends AbstractSystemEntityTest {

  @Autowired StyleSheetMetadata metadata;
  @Autowired StyleSheetFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, StyleSheet.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
