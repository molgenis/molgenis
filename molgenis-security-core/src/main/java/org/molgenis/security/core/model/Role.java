package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;

/**
 * Represents a Role.
 */
@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class Role
{
	public abstract String getId();

	public abstract String getLabel();

	public static Role.Builder builder()
	{
		return new AutoValue_Role.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Role.Builder id(String id);

		public abstract Role.Builder label(String label);

		public abstract Role build();
	}
}
