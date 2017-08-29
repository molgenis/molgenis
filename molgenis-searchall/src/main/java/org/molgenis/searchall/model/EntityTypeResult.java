package org.molgenis.searchall.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResult.class)
public abstract class EntityTypeResult
{
	public abstract String getId();

	public abstract String getLabel();

	@Nullable
	public abstract String getDescription();

	public abstract String getPackageId();

	public abstract boolean isLabelMatch();

	public abstract boolean isDescriptionMatch();

	public abstract List<AttributeResult> getAttributes();

	public abstract long nrOfMatchingEntities();

	public static EntityTypeResult create(String id, String label, String description, String packageId,
			boolean isLabelMatch, boolean isDescriptionMatch, List<AttributeResult> attributes,
			long nrOfMatchingEntities)
	{
		return new AutoValue_EntityTypeResult(id, label, description, packageId, isLabelMatch, isDescriptionMatch,
				attributes, nrOfMatchingEntities);
	}
}
