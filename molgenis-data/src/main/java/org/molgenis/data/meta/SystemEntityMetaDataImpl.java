package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SystemEntityMetaDataImpl extends EntityMetaDataImpl implements SystemEntityMetaData
{
	private AttributeMetaDataFactory attributeMetaDataFactory;

	protected SystemEntityMetaDataImpl()
	{
		super();
	}

	public SystemEntityMetaDataImpl(String entityName)
	{
		super(entityName);
	}

	public SystemEntityMetaDataImpl(String entityName, Class<? extends Entity> entityClass)
	{
		super(entityName, entityClass);
	}

	public SystemEntityMetaDataImpl(EntityMetaData entityMetaData)
	{
		super(entityMetaData);
	}

	@Override
	public AttributeMetaData addAttribute(String attrName, AttributeRole... attrTypes)
	{
		return addAttribute(attrName, null, attrTypes);
	}

	@Override
	public AttributeMetaData addAttribute(String attrName, AttributeMetaData parentAttr, AttributeRole... attrTypes)
	{
		AttributeMetaData attr = attributeMetaDataFactory.create();
		attr.setName(attrName);
		if (parentAttr != null)
		{
			parentAttr.addAttributePart(attr);
			// FIXME assign roles, see super.addAttribute(AttributeMetaData attr, AttributeRole... attrTypes)
		}
		else
		{
			super.addAttribute(attr, attrTypes);
		}
		return attr;
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataFactory(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);
	}
}
