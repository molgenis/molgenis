package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.omx.core.Identifiable;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;

public class IdentifiableDecorator<E extends Identifiable> extends MapperDecorator<E>
{
	public IdentifiableDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		return super.add(entities);
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{
		return super.update(entities);
	}
}
