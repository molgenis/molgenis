package org.molgenis.core.ui.settings;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, StaticContentMetadata.class, StaticContentFactory.class})
public class StaticContentTest extends AbstractSystemEntityTest {

  @Autowired StaticContentMetadata metadata;
  @Autowired StaticContentFactory factory;

  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata,
        StaticContentMetadata.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs(),
        true);
  }
}
