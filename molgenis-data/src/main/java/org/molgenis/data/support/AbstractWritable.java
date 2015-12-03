package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

public abstract class AbstractWritable implements Writable
{
	public enum EntityWriteMode
	{
		ENTITY_LABELS, ENTITY_IDS
	}

	public enum AttributeWriteMode
	{
		ATTRIBUTE_NAMES, ATTRIBUTE_LABELS
	}

	private EntityWriteMode entityWriteMode;
	private AttributeWriteMode attributeWriteMode;

	public EntityWriteMode getEntityWriteMode()
	{
		return entityWriteMode;
	}

	public void setEntityWriteMode(EntityWriteMode entityWriteMode)
	{
		this.entityWriteMode = entityWriteMode;
	}

	public AttributeWriteMode getAttributeWriteMode()
	{
		return attributeWriteMode;
	}

	public void setAttributeWriteMode(AttributeWriteMode attributeWriteMode)
	{
		this.attributeWriteMode = attributeWriteMode;
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		Integer count = 0;
		for (Entity entity : entities)
		{
			add(entity);
			count++;
		}
		return count;
	}
}
