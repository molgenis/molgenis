package org.molgenis.script.core;

import org.molgenis.data.meta.AbstractEntityFactoryTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptFactoryTest extends AbstractEntityFactoryTest {

  @Autowired ScriptFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, Script.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, Script.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, Script.class);
  }
}
