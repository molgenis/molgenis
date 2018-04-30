package org.molgenis.data.security;

import org.molgenis.security.core.Permission;

/**
 * Permission to perform an action on an entity instance.
 */
public enum EntityPermission implements Permission
{
	// @formatter:off
	READ("Permission to read this entity"),
	UPDATE("Permission to update this entity"),
	DELETE("Permission to delete this entity");
	// @formatter:on

	private final String defaultDescription;

	EntityPermission(String defaultDescription)
	{
		this.defaultDescription = defaultDescription;
	}

	@Override
	public String getDefaultDescription()
	{
		return defaultDescription;
	}
}
