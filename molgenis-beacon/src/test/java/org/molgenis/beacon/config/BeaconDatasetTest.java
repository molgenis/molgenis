package org.molgenis.beacon.config;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      BeaconDatasetMetadata.class,
      BeaconDatasetFactory.class,
      BeaconPackage.class,
      BeaconTestConfig.class
    })
class BeaconDatasetTest extends AbstractSystemEntityTest {

  @Autowired BeaconDatasetMetadata metadata;
  @Autowired BeaconDatasetFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, BeaconDataset.class, factory, getOverriddenReturnTypes(), getExcludedAttrs());
  }
}
