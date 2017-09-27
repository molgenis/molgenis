package org.molgenis.data.postgresql.identifier;

import java.util.List;

public interface EntityTypeRegistry
{
	void registerEntityType(String entityTypeId, List<Identifiable> referenceTypeAttributes);

	void unregisterEntityType(String entityTypeId, List<Identifiable> referenceTypeAttributes);

	EntityTypeDescription getEntityTypeDescription(String tableOrJunctionTableName);
}
