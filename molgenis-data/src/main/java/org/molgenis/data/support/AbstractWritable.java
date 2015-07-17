package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

public abstract class AbstractWritable implements Writable
{
	public enum WriteMode
	{
		ENTITY_LABELS, ENTITY_IDS
	}

	private WriteMode writeMode;

	public WriteMode getWriteMode()
	{
		return writeMode;
	}

	public void setWriteMode(WriteMode writeMode)
	{
		this.writeMode = writeMode;
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
