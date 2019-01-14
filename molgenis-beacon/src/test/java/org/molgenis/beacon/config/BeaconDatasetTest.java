package org.molgenis.beacon.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      BeaconDatasetMetadata.class,
      BeaconDatasetFactory.class,
      BeaconPackage.class,
      BeaconTestConfig.class
    })
public class BeaconDatasetTest extends AbstractSystemEntityTest {

  @Autowired BeaconDatasetMetadata metadata;
  @Autowired BeaconDatasetFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, BeaconDataset.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
