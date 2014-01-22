package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

public abstract class AbstractWritable implements Writable
{

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		for (Entity entity : entities)
		{
			add(entity);
		}
	}

}
