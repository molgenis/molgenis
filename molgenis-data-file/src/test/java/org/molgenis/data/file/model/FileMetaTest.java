package org.molgenis.data.file.model;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {EntityBaseTestConfig.class, FileMetaMetadata.class, FileMetaFactory.class})
public class FileMetaTest extends AbstractSystemEntityTest {

  @Autowired FileMetaMetadata metadata;
  @Autowired FileMetaFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, FileMeta.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
