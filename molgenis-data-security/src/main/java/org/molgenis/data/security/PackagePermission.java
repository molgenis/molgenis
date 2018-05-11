package org.molgenis.data.security;

import org.molgenis.security.core.Permission;

public enum PackagePermission implements Permission
{
	// @formatter:off
	ADD_ENTITY_TYPE("Permission to add a child entity type to this package"),
	ADD_PACKAGE("Permission to add a child package to this package"),
	UPDATE("Permission to update this package");
	// @formatter:on

	private final String defaultDescription;

	PackagePermission(String defaultDescription)
	{
		this.defaultDescription = defaultDescription;
	}

	@Override
	public String getDefaultDescription()
	{
		return defaultDescription;
	}
}