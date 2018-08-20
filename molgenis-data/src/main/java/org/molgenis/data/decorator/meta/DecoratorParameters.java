package org.molgenis.data.decorator.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.decorator.meta.DecoratorParametersMetadata.*;

public class DecoratorParameters extends StaticEntity
{
	public DecoratorParameters(Entity entity)
	{
		super(entity);
	}

	public DecoratorParameters(EntityType entityType)
	{
		super(entityType);
	}

	public DecoratorParameters(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public DynamicDecorator getDecorator()
	{
		return getEntity(DECORATOR, DynamicDecorator.class);
	}

	public String getParameters()
	{
		return getString(PARAMETERS);
	}
}
