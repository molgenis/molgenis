package org.molgenis.beacon.config;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.beacon.config.BeaconMetadata.*;

public class Beacon extends StaticEntity
{
	public Beacon(Entity entity)
	{
		super(entity);
	}

	public Beacon(EntityType entityType)
	{
		super(entityType);
	}

	public Beacon(String identifier, EntityType entityType)
	{
		super(identifier, entityType);
	}

	public String getId()
	{
		return getString(ID);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public String getApiVersion()
	{
		return getString(API_VERSION);
	}

	@Nullable
	public BeaconOrganization getOrganization()
	{
		return getEntity(BEACON_ORGANIZATION, BeaconOrganization.class);
	}

	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	@Nullable
	public String getVersion()
	{
		return getString(VERSION);
	}

	@Nullable
	public String getWelcomeUrl()
	{
		return getString(WELCOME_URL);
	}

	public Iterable<EntityType> getDataSets()
	{
		return getEntities(DATA_SETS, EntityType.class);
	}
}
