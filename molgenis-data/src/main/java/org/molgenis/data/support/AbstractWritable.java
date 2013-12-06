package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

public abstract class AbstractWritable<E extends Entity> implements Writable<E>
{

	@Override
	public void add(Iterable<E> entities)
	{
		for (E entity : entities)
		{
			add(entity);
		}
	}

}
