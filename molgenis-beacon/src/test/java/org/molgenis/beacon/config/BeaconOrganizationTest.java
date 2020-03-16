package org.molgenis.beacon.config;

import org.junit.jupiter.api.Test;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      BeaconOrganizationMetadata.class,
      BeaconOrganizationFactory.class,
      BeaconPackage.class
    })
class BeaconOrganizationTest extends AbstractSystemEntityTest {

  @Autowired BeaconOrganizationMetadata metadata;
  @Autowired BeaconOrganizationFactory factory;

  @SuppressWarnings("java:S2699") // Tests should include assertions
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
