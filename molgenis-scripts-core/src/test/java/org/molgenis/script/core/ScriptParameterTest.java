package org.molgenis.script.core;

import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptParameterTest extends AbstractSystemEntityTest {

  @Autowired ScriptParameterMetadata metadata;
  @Autowired ScriptParameterFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ScriptParameter.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
