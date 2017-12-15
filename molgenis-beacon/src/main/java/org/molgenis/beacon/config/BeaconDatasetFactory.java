package org.molgenis.beacon.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class BeaconDatasetFactory extends AbstractSystemEntityFactory<BeaconDataset, BeaconDatasetMetadata, String>
{
	BeaconDatasetFactory(BeaconDatasetMetadata beaconDatasetMetadata, EntityPopulator entityPopulator)
	{
		super(BeaconDataset.class, beaconDatasetMetadata, entityPopulator);
	}
}
