package org.molgenis.core.ui.data.system.core;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      FreemarkerTemplateMetadata.class,
      FreemarkerTemplateFactory.class,
      RootSystemPackage.class
    })
public class FreemarkerTemplateFactoryTest extends AbstractEntityFactoryTest {

  @Autowired FreemarkerTemplateFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, FreemarkerTemplate.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, FreemarkerTemplate.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, FreemarkerTemplate.class);
  }
}
