package org.molgenis.beacon.config;

import org.molgenis.beacon.controller.model.BeaconOrganizationResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.beacon.config.BeaconOrganizationMetadata.*;

public class BeaconOrganization extends StaticEntity
{
	public BeaconOrganization(Entity entity)
	{
		super(entity);
	}

	public BeaconOrganization(EntityType entityType)
	{
		super(entityType);
	}

	public BeaconOrganization(String identifier, EntityType entityType)
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

	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	@Nullable
	public String getAddress()
	{
		return getString(ADDRESS);
	}

	@Nullable
	public String getWelcomeUrl()
	{
		return getString(WELCOME_URL);
	}

	@Nullable
	public String getContactUrl()
	{
		return getString(CONTACT_URL);
	}

	@Nullable
	public String getLogoUrl()
	{
		return getString(LOGO_URL);
	}

	public BeaconOrganizationResponse toBeaconOrganizationResponse()
	{
		return BeaconOrganizationResponse.create(getId(), getName(), getDescription(), getAddress(), getWelcomeUrl(),
				getContactUrl(), getLogoUrl());
	}
}
