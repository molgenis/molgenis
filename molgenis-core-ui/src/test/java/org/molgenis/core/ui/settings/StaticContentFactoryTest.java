package org.molgenis.core.ui.settings;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, StaticContentMetadata.class, StaticContentFactory.class})
public class StaticContentFactoryTest extends AbstractEntityFactoryTest {

  @Autowired StaticContentFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, StaticContent.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, StaticContent.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, StaticContent.class);
  }
}
