package org.molgenis.data.meta.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * {@link AttributeMetaData Attribute meta data} factory. This factory does not extend from
 * {@link AbstractSystemEntityFactory} to prevent a circular bean dependency.
 */
@Component
public class AttributeMetaDataFactory implements EntityFactory<AttributeMetaData, String>
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
	public AttributeMetaData create()
	{
		AttributeMetaData attrMeta = new AttributeMetaData(attrMetaMeta);
		entityPopulator.populate(attrMeta);
		return attrMeta;
	}

	@Override
	public AttributeMetaData create(String entityId)
	{
		AttributeMetaData attrMeta = create();
		attrMeta.set(AttributeMetaDataMetaData.IDENTIFIER, entityId);
		return attrMeta;
	}

	@Override
	public AttributeMetaData create(Entity entity)
	{
		return new AttributeMetaData(entity);
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