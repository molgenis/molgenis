package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;

public abstract class AbstractWritable implements Writable
{

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
