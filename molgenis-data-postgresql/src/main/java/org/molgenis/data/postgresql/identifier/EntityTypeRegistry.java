package org.molgenis.data.postgresql.identifier;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

public interface EntityTypeRegistry
{
	void registerEntityType(EntityType entityType);

	void unregisterEntityType(EntityType entityType);

	void addAttribute(EntityType entityType, Attribute attribute);

	void updateAttribute(EntityType entityType, Attribute attr, Attribute updatedAttr);

	void deleteAttribute(EntityType entityType, Attribute attr);

	EntityTypeDescription getEntityTypeDescription(String tableOrJunctionTableName);
}
