package org.molgenis.data.importer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ImportRunMetadata.class,
      ImportRunFactory.class,
      ImportTestConfig.class
    })
class ImportRunTest extends AbstractSystemEntityTest {

  @Autowired ImportRunMetadata metadata;
  @Autowired ImportRunFactory factory;

  private ImportRun importRun;

  @BeforeEach
  void setUp() {
    importRun = factory.create();
  }

  @Test
  void testGetNotifyDefaultFalse() throws Exception {
    assertFalse(importRun.getNotify());
  }

  @Test
  void testSetNotify() throws Exception {
    importRun.setNotify(true);
    assertTrue(importRun.getNotify());
  }

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, ImportRun.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
