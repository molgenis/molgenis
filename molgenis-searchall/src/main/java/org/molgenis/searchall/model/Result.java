package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Result.class)
public abstract class Result
{
	public abstract ImmutableList<EntityTypeResult> getEntityTypes();

	public abstract ImmutableList<PackageResult> getPackages();

	@AutoValue.Builder
	public abstract static class Builder {
		public abstract Builder setEntityTypes(List<EntityTypeResult> entityTypes);
		public abstract Builder setPackages(List<PackageResult> packages);
		public abstract Result build();
	}

	public static Builder builder() {
		return new AutoValue_Result.Builder();
	}
}
