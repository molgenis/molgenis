package org.molgenis.script.core;

import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptTypeTest extends AbstractSystemEntityTest {

  @Autowired ScriptTypeMetadata metadata;
  @Autowired ScriptTypeFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ScriptType.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
