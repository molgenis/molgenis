package org.molgenis.ngs.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.ngs.Flowcell;

/**
 * Generic decorator for NGS Flowcell values. Used to alert the user when values
 * do not meet certain criteria.
 * 
 * @version 1.0.0.0
 * 
 * @author Marcel Burger
 * 
 * @param <E>
 */

public class FlowcellDecorator<E extends Flowcell> extends MapperDecorator<E>
{

	public FlowcellDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (E entity : entities)
			validate(entity);
		return super.add(entities);
	}

	private void validate(E entity) throws DatabaseException
	{
		if (!entity.getRun().matches("\\d4")) throw new DatabaseException("Run must be four numbers");
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		// TODO implement
		return super.update(entities);
	}
}
