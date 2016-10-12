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
public class AttributeMetaDataFactory implements EntityFactory<Attribute, String>
{
	private final EntityPopulator entityPopulator;
	private AttributeMetaDataMetaData attrMetaMeta;

	@Autowired
	public AttributeMetaDataFactory(EntityPopulator entityPopulator) {
		this.entityPopulator = requireNonNull(entityPopulator);
	}

	@Override
	public String getEntityName()
	{
		return AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
	}

	@Override
	public Attribute create()
	{
		Attribute attrMeta = new Attribute(attrMetaMeta);
		entityPopulator.populate(attrMeta);
		return attrMeta;
	}

	@Override
	public Attribute create(String entityId)
	{
		Attribute attrMeta = create();
		attrMeta.set(AttributeMetaDataMetaData.IDENTIFIER, entityId);
		return attrMeta;
	}

	@Override
	public Attribute create(Entity entity)
	{
		return new Attribute(entity);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataMetaData(AttributeMetaDataMetaData attrMetaMeta)
	{
		this.attrMetaMeta = requireNonNull(attrMetaMeta);
	}

	public AttributeMetaDataMetaData getAttributeMetaDataMetaData()
	{
		return attrMetaMeta;
	}
}