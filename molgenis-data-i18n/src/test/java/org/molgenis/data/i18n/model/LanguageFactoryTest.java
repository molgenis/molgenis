package org.molgenis.data.i18n.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, LanguageMetadata.class, LanguageFactory.class})
public class LanguageFactoryTest extends AbstractEntityFactoryTest {

  @Autowired LanguageFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Language.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Language.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Language.class);
  }
}
