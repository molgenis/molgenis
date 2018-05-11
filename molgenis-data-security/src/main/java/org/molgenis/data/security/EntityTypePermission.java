package org.molgenis.data.security;

import org.molgenis.security.core.Permission;

/**
 * Permission to perform an action on an EntityType.
 */
public enum EntityTypePermission implements Permission
{
	// @formatter:off
	READ_METADATA("Permission to read the metadata of this EntityType"),
	COUNT_DATA("Permission to count entities of this EntityType"),
	AGGREGATE_DATA("Permission to aggregate entities of this EntityType"),
	READ_DATA("Permission to read entities of this EntityType"),
	ADD_DATA("Permission to add entities of this EntityType"),
	UPDATE_DATA("Permission to update entities of this EntityType"),
	DELETE_DATA("Permission to delete entities of this EntityType"),
	UPDATE_METADATA("Permission to update the metadata of this EntityType"),
	DELETE_METADATA("Permission to delete the data and metadata of this EntityType");
	// @formatter:on

	private final String defaultDescription;

	EntityTypePermission(String defaultDescription)
	{
		this.defaultDescription = defaultDescription;
	}

	@Override
	public String getDefaultDescription()
	{
		return defaultDescription;
	}
}
