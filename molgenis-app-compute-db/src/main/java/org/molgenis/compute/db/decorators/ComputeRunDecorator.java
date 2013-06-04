package org.molgenis.compute.db.decorators;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;

public class ComputeRunDecorator<E extends ComputeRun> extends MapperDecorator<E>
{
	public ComputeRunDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		validate(entities);
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		validate(entities);
		return super.update(entities);
	}

	private void validate(List<E> entities) throws DatabaseException
	{
		for (ComputeRun run : entities)
		{
			if (StringUtils.isEmpty(run.getName()) || !StringUtils.isAlphanumeric(run.getName()))
			{
				throw new ComputeDbException("Illegal run name [" + run.getName() + "] should be non empty alphnumeric");
			}

			if ((run.getPollDelay() != null) && (run.getPollDelay() < 2000))
			{
				throw new ComputeDbException("Illegal pollDelay value. Should be bigger than 2000ms");
			}
		}

	}

}
