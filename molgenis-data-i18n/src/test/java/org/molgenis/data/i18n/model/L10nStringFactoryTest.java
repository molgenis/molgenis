package org.molgenis.data.i18n.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, L10nStringMetadata.class, L10nStringFactory.class})
public class L10nStringFactoryTest extends AbstractEntityFactoryTest {

  @Autowired L10nStringFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, L10nString.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, L10nString.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, L10nString.class);
  }
}
