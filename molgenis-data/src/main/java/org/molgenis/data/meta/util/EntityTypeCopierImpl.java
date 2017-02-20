package org.molgenis.data.meta.util;

import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;

@Component
public class EntityTypeCopierImpl implements EntityTypeCopier
{
	@Override
	public EntityType copy(EntityType entityType)
	{
		return EntityType.newInstance(entityType);
	}
}
