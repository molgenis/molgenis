package org.molgenis.script.core;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptTypeFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScriptTypeFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ScriptType.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ScriptType.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ScriptType.class);
  }
}
