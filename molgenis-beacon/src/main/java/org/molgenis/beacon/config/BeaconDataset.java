package org.molgenis.beacon.config;

import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;

import static org.molgenis.beacon.config.BeaconDatasetMetadata.*;
import static org.molgenis.beacon.config.BeaconMetadata.DESCRIPTION;
import static org.molgenis.beacon.config.BeaconMetadata.ID;

public class BeaconDataset extends StaticEntity
{
	public BeaconDataset(Entity entity)
	{
		super(entity);
	}

	public BeaconDataset(EntityType entityType)
	{
		super(entityType);
	}

	public BeaconDataset(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
	}

	public String getId()
	{
		return getString(ID);
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public EntityType getEntityType()
	{
		return getEntity(ENTITY_TYPE, EntityType.class);
	}

	public GenomeBrowserAttributes getGenomeBrowserAttributes()
	{
		return getEntity(GENOME_BROWSER_ATTRIBUTES, GenomeBrowserAttributes.class);
	}

	public BeaconDatasetResponse toBeaconDatasetResponse()
	{
		return BeaconDatasetResponse.create(getId(), getLabel(), getDescription());
	}

}
