package org.molgenis.data.index.meta;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, IndexActionGroup.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
