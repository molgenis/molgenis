package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

@AutoValue
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityTypeSearchResults
{
	public abstract EntityType getEntityType();

	public abstract List<AttributeSearchResults> getAttributeSearchResults();

	public static EntityTypeSearchResults create(EntityType entityType,
			List<AttributeSearchResults> attributeSearchResults)
	{
		return new AutoValue_EntityTypeSearchResults(entityType, attributeSearchResults);
	}
}
