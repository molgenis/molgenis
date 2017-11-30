package org.molgenis.beacon.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class BeaconFactory extends AbstractSystemEntityFactory<Beacon, BeaconMetadata, String>
{
	BeaconFactory(BeaconMetadata beaconMetadata, EntityPopulator entityPopulator)
	{
		super(Beacon.class, beaconMetadata, entityPopulator);
	}
}