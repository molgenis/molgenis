package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;

/**
 * Represents a Role.
 */
@AutoValue
public abstract class Role
{
	public abstract String getId();

	public abstract String getLabel();

	public static Role create(String id, String label)
	{
		return new AutoValue_Role(id, label);
	}
}
