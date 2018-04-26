package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeFactory extends AbstractSystemEntityFactory<EntityType, EntityTypeMetadata, String>
{
	EntityTypeFactory(EntityTypeMetadata entityTypeMeta, EntityPopulator entityPopulator)
	{
		super(EntityType.class, entityTypeMeta, entityPopulator);
	}
}
