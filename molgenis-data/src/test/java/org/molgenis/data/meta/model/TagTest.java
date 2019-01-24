package org.molgenis.data.meta.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Tag.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
