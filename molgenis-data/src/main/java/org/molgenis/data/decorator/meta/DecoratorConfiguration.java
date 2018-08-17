package org.molgenis.data.decorator.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.*;

public class DecoratorConfiguration extends StaticEntity
{
	public DecoratorConfiguration(Entity entity)
	{
		super(entity);
	}

	public DecoratorConfiguration(EntityType entityType)
	{
		super(entityType);
	}

	public String getEntityTypeId()
	{
		return getString(ENTITY_TYPE_ID);
	}

	public void setEntityTypeId(String entityTypeId)
	{
		set(ENTITY_TYPE_ID, entityTypeId);
	}

	public Stream<DynamicDecorator> getDecorators()
	{
		return StreamSupport.stream(getEntities(DYNAMIC_DECORATORS, DynamicDecorator.class).spliterator(), false);
	}

	public void setDecorators(List<DynamicDecorator> decorators)
	{
		set(DYNAMIC_DECORATORS, decorators);
	}

	public JsonElement getParameters()
	{
		return new JsonParser().parse(getString(PARAMETERS));
	}
}
