package org.molgenis.file;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.platform.DataAccessExceptionTranslator;
import org.molgenis.data.platform.ExceptionTranslatorRepositoryDecorator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class ExceptionTranslatorRepositoryDecoratorFactory
{
	private final DataAccessExceptionTranslator dataAccessExceptionTranslator;

	public ExceptionTranslatorRepositoryDecoratorFactory(DataAccessExceptionTranslator dataAccessExceptionTranslator)
	{
		this.dataAccessExceptionTranslator = requireNonNull(dataAccessExceptionTranslator);
	}

	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		return new ExceptionTranslatorRepositoryDecorator(repository, dataAccessExceptionTranslator);
	}
}
