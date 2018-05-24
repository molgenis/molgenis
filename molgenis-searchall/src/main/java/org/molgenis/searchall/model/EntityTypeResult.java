package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResult.class)
public abstract class EntityTypeResult implements Described
{
	public abstract String getId();

	@Nullable
	public abstract String getPackageId();

	public abstract boolean isLabelMatch();

	public abstract boolean isDescriptionMatch();

	public abstract ImmutableList<AttributeResult> getAttributes();

	public abstract long getNrOfMatchingEntities();

	public boolean isMatch()
	{
		return isLabelMatch() || isDescriptionMatch() || !getAttributes().isEmpty() || getNrOfMatchingEntities() > 0;
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setId(String id);

		public abstract Builder setLabel(String label);

		public abstract Builder setDescription(String description);

		public abstract Builder setPackageId(String packageId);

		public abstract Builder setLabelMatch(boolean labelMatch);

		public abstract Builder setDescriptionMatch(boolean descriptionMatch);

		public abstract Builder setAttributes(List<AttributeResult> attributes);

		public abstract Builder setNrOfMatchingEntities(long nrOfMatchingEntites);

		public abstract EntityTypeResult build();
	}

	public static Builder builder()
	{
		return new AutoValue_EntityTypeResult.Builder();
	}
}
