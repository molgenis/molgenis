package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
	public Integer add(Stream<? extends Entity> entities)
	{
		AtomicInteger count = new AtomicInteger(0);
		entities.forEach(entity ->
		{
			add(entity);
			count.incrementAndGet();
		});
		return count.get();
	}
}
