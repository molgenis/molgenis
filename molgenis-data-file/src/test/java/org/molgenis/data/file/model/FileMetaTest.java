package org.molgenis.data.file.model;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, FileMetaMetadata.class, FileMetaFactory.class})
public class FileMetaTest extends AbstractSystemEntityTest {

  @Autowired FileMetaMetadata metadata;
  @Autowired FileMetaFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, FileMeta.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
