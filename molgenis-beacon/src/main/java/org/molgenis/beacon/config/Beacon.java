package org.molgenis.beacon.config;

import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.beacon.controller.model.BeaconOrganizationResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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

	public Iterable<BeaconDataset> getDataSets()
	{
		return getEntities(DATA_SETS, BeaconDataset.class);
	}

	public BeaconResponse toBeaconResponse()
	{
		BeaconOrganizationResponse beaconOrganizationResponse;
		if (getOrganization() == null)
		{
			beaconOrganizationResponse = null;
		}
		else
		{
			beaconOrganizationResponse = getOrganization().toBeaconOrganizationResponse();
		}

		return BeaconResponse.create(getId(), getName(), getApiVersion(), beaconOrganizationResponse, getDescription(),
				getVersion(), getWelcomeUrl(), entityTypeToBeaconDataset());
	}

	private List<BeaconDatasetResponse> entityTypeToBeaconDataset()
	{
		List<BeaconDatasetResponse> beaconDatasets = newArrayList();
		getDataSets().forEach(dataset -> beaconDatasets.add(
				BeaconDatasetResponse.create(dataset.getId(), dataset.getLabel(), dataset.getDescription())));
		return beaconDatasets;
	}
}
