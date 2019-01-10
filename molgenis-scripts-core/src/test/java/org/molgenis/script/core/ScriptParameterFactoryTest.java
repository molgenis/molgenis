package org.molgenis.script.core;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptParameterFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScriptParameterFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, ScriptParameter.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, ScriptParameter.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, ScriptParameter.class);
  }
}
