package org.molgenis.data.decorator.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_PARAMETERS;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.ENTITY_TYPE_ID;

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

	public Stream<DecoratorParameters> getDecoratorParameters()
	{
		return stream(getEntities(DECORATOR_PARAMETERS, DecoratorParameters.class).spliterator(), false);
	}

	public void setDecoratorParameters(Stream<DecoratorParameters> parameters)
	{
		set(DECORATOR_PARAMETERS, parameters.collect(toList()));
	}
}
