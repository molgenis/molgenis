package org.molgenis.data.decorator.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.NAME;

/**
 * Entity representation of a dynamic decorator
 */
public class DynamicDecorator extends StaticEntity
{
	public DynamicDecorator(Entity entity)
	{
		super(entity);
	}

	public DynamicDecorator(EntityType entityType)
	{
		super(entityType);
	}

	public DynamicDecorator(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}
}
