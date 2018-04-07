package org.molgenis.apps;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class AppInfoDto
{
	public abstract String getId();

	public abstract String getName();

	@Nullable
	public abstract String getDescription();

	public abstract boolean isActive();

	public static Builder builder()
	{
		return new AutoValue_AppInfoDto.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setId(String newId);

		public abstract Builder setName(String newName);

		public abstract Builder setDescription(String newDescription);

		public abstract Builder setActive(boolean newActive);

		public abstract AppInfoDto build();
	}
}
