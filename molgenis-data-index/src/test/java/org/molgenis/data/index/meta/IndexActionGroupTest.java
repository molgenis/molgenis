package org.molgenis.data.index.meta;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      IndexActionGroupMetadata.class,
      IndexActionGroupFactory.class,
      IndexPackage.class
    })
public class IndexActionGroupTest extends AbstractSystemEntityTest {

  @Autowired IndexActionGroupMetadata metadata;
  @Autowired IndexActionGroupFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, IndexActionGroup.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
