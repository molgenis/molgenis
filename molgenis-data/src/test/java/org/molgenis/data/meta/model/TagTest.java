package org.molgenis.data.meta.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      TagMetadata.class,
      TagFactory.class,
      MetadataTestConfig.class
    })
public class TagTest extends AbstractSystemEntityTest {

  @Autowired TagMetadata metadata;
  @Autowired TagFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Tag.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
