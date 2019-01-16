package org.molgenis.data.i18n.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, L10nStringMetadata.class, L10nStringFactory.class})
public class L10nStringTest extends AbstractSystemEntityTest {

  @Autowired L10nStringMetadata metadata;
  @Autowired L10nStringFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, L10nString.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
