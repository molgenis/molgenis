package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.IDENTIFIER;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link AttributeMetaData Attribute meta data} factory. This factory does not extend from
 * {@link org.molgenis.data.AbstractEntityFactory} to prevent a circular bean dependency.
 */
@Component
public class AttributeMetaDataFactory implements EntityFactory<AttributeMetaData, String>
{
	private AttributeMetaDataMetaData attrMetaMeta;

	@Override
	public AttributeMetaData create()
	{
		return new AttributeMetaData(attrMetaMeta);
	}

	@Override
	public AttributeMetaData create(String entityId)
	{
		AttributeMetaData attrMeta = create();
		attrMeta.set(IDENTIFIER, entityId);
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
}