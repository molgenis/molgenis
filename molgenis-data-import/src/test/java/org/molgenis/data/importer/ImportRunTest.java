package org.molgenis.data.importer;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      ImportRunMetadata.class,
      ImportRunFactory.class,
      ImportTestConfig.class
    })
public class ImportRunTest extends AbstractSystemEntityTest {

  @Autowired ImportRunMetadata metadata;
  @Autowired ImportRunFactory factory;

  private ImportRun importRun;

  @BeforeClass
  public void setUp() {
    importRun = factory.create();
  }

  @Test
  public void testGetNotifyDefaultFalse() throws Exception {
    assertFalse(importRun.getNotify());
  }

  @Test
  public void testSetNotify() throws Exception {
    importRun.setNotify(true);
    assertTrue(importRun.getNotify());
  }

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, ImportRun.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }
}
