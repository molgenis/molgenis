package org.molgenis.script.core;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptParameterTest extends AbstractSystemEntityTest {

  @Autowired ScriptParameterMetadata metadata;
  @Autowired ScriptParameterFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, ScriptParameter.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
