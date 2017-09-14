package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * {@link Attribute Attribute meta data} factory. This factory does not extend from
 * {@link AbstractSystemEntityFactory} to prevent a circular bean dependency.
 */
@Component
public class AttributeFactory implements EntityFactory<Attribute, String>
{
	private final EntityPopulator entityPopulator;
	private AttributeMetadata attributeMetadata;

	public AttributeFactory(EntityPopulator entityPopulator)
	{
		this.entityPopulator = requireNonNull(entityPopulator);
	}

	@Override
	public String getEntityTypeId()
	{
		return AttributeMetadata.ATTRIBUTE_META_DATA;
	}

	@Override
	public Attribute create()
	{
		Attribute attribute = new Attribute(attributeMetadata);
		entityPopulator.populate(attribute);
		return attribute;
	}

	@Override
	public Attribute create(String entityId)
	{
		Attribute attribute = create();
		attribute.set(AttributeMetadata.ID, entityId);
		return attribute;
	}

	@Override
	public Attribute create(Entity entity)
	{
		return new Attribute(entity);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetadata(AttributeMetadata attributeMetadata)
	{
		this.attributeMetadata = requireNonNull(attributeMetadata);
	}

	public AttributeMetadata getAttributeMetadata()
	{
		return attributeMetadata;
	}
}