package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SortOrder
{
	public abstract String getField();

	public abstract SortDirection getDirection();

	public static SortOrder create(String newField, SortDirection newDirection)
	{
		return builder().setField(newField).setDirection(newDirection).build();
	}

	public static Builder builder()
	{
		return new AutoValue_SortOrder.Builder().setDirection(SortDirection.ASC);
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setField(String newField);

		public abstract Builder setDirection(SortDirection newDirection);

		public abstract SortOrder build();
	}
}
