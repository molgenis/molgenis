package org.molgenis.core.ui.style;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.file.model.FileMetaMetadata;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.settings.SettingsPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      StyleSheetMetadata.class,
      StyleSheetFactory.class,
      SettingsPackage.class,
      FileMetaMetadata.class
    })
public class StyleSheetFactoryTest extends AbstractEntityFactoryTest {

  @Autowired StyleSheetFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, StyleSheet.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, StyleSheet.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, StyleSheet.class);
  }
}
