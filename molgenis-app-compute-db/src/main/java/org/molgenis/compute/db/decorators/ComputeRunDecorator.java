package org.molgenis.compute.db.decorators;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;

public class ComputeRunDecorator extends MapperDecorator<ComputeRun>
{
	public ComputeRunDecorator(Mapper<ComputeRun> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<ComputeRun> entities) throws DatabaseException
	{
		validate(entities);
		return super.add(entities);
	}

	@Override
	public int update(List<ComputeRun> entities) throws DatabaseException
	{
		validate(entities);
		return super.update(entities);
	}

	private void validate(List<ComputeRun> entities) throws DatabaseException
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
