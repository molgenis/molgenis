package org.molgenis.beacon.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractEntityFactoryTest;
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
public class BeaconDatasetFactoryTest extends AbstractEntityFactoryTest {

  @Autowired BeaconDatasetFactory factory;

  @Override
  @Test
  public void testCreate() {
    super.testCreate(factory, BeaconDataset.class);
  }

  @Override
  @Test
  public void testCreateWithId() {
    super.testCreateWithId(factory, BeaconDataset.class);
  }

  @Override
  @Test
  public void testCreateWithEntity() {
    super.testCreateWithEntity(factory, BeaconDataset.class);
  }
}
