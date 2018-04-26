package org.molgenis.data.importer;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class PersistResult
{
	public abstract ImmutableMap<String, Long> getNrPersistedEntitiesMap();

	public static PersistResult create(ImmutableMap<String, Long> newNrPersistedEntitiesMap)
	{
		return new AutoValue_PersistResult(newNrPersistedEntitiesMap);
	}
}
