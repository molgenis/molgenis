package org.molgenis.data.decorator.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.decorator.meta.DynamicDecoratorMetadata.*;

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

	public DynamicDecorator(String id, EntityType entityType)
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

	public DynamicDecorator setLabel(String id)
	{
		set(LABEL, id);
		return this;
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public DynamicDecorator setDescription(String id)
	{
		set(DESCRIPTION, id);
		return this;
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}
}
