package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

import java.util.List;

import static java.util.Collections.emptyList;

@AutoValue
public abstract class Mapping
{
	public abstract String getType();

	public abstract List<FieldMapping> getFieldMappings();

	public static Mapping create(String newType, List<FieldMapping> newFieldMappings)
	{
		return builder().setType(newType).setFieldMappings(newFieldMappings).build();
	}

	public static Builder builder()
	{
		return new AutoValue_Mapping.Builder().setFieldMappings(emptyList());
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setType(String newType);

		public abstract Builder setFieldMappings(List<FieldMapping> newFieldMappings);

		public abstract Mapping build();
	}
}
