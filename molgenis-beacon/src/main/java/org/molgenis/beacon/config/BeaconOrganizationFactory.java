package org.molgenis.beacon.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class BeaconOrganizationFactory
		extends AbstractSystemEntityFactory<BeaconOrganization, BeaconOrganizationMetadata, String>
{
	BeaconOrganizationFactory(BeaconOrganizationMetadata beaconOrganizationMetadata, EntityPopulator entityPopulator)
	{
		super(BeaconOrganization.class, beaconOrganizationMetadata, entityPopulator);
	}
}
