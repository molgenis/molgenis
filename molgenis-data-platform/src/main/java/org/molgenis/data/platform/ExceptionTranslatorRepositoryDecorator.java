package org.molgenis.data.platform;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.springframework.dao.DataAccessException;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Translate repository operation {@link DataAccessException} to {@link org.molgenis.util.LocalizedRuntimeException}.
 */
public class ExceptionTranslatorRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final DataAccessExceptionTranslator dataAccessExceptionTranslator;

	public ExceptionTranslatorRepositoryDecorator(Repository<Entity> delegateRepository,
			DataAccessExceptionTranslator dataAccessExceptionTranslator)
	{
		super(delegateRepository);
		this.dataAccessExceptionTranslator = requireNonNull(dataAccessExceptionTranslator);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		try
		{
			return super.add(entities);
		}
		catch (DataAccessException e)
		{
			throw dataAccessExceptionTranslator.translate(e);
		}
	}

	// TODO decorate all CRUD methods similar to add(Stream)
}
