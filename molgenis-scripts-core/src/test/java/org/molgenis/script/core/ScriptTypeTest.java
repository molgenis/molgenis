package org.molgenis.script.core;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.script.core.config.ScriptTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {ScriptTestConfig.class})
public class ScriptTypeTest extends AbstractSystemEntityTest {

  @Autowired ScriptTypeMetadata metadata;
  @Autowired ScriptTypeFactory factory;

  @SuppressWarnings("squid:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, ScriptType.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
