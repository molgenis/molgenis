package org.molgenis.data.postgresql.identifier;

import org.molgenis.data.meta.model.EntityType;

public interface EntityTypeRegistry
{
	void registerEntityType(EntityType entityType);

	void unregisterEntityType(EntityType entityType);

	EntityTypeDescription getEntityTypeDescription(String tableOrJunctionTableName);
}
