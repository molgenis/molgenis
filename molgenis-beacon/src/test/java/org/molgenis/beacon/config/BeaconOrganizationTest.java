package org.molgenis.beacon.config;

import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      BeaconOrganizationMetadata.class,
      BeaconOrganizationFactory.class,
      BeaconPackage.class
    })
public class BeaconOrganizationTest extends AbstractSystemEntityTest {

  @Autowired BeaconOrganizationMetadata metadata;
  @Autowired BeaconOrganizationFactory factory;

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata,
        BeaconOrganization.class,
        factory,
        getOverriddenReturnTypes(),
        getExcludedAttrs());
  }
}
