package org.molgenis.data.postgresql.identifier;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@AutoValue
public abstract class EntityTypeDescription
{
	public abstract String getId();

	public abstract ImmutableMap<String, AttributeDescription> getAttributeDescriptionMap();

	public static EntityTypeDescription create(String id, Map<String, AttributeDescription> attrDescriptionMap)
	{
		return new AutoValue_EntityTypeDescription(id, ImmutableMap.copyOf(attrDescriptionMap));
	}
}
