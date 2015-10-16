package org.molgenis.data.jpa.importer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractEntitiesImporter implements EntitiesImporter
{
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final EntityImportService entityImportService;

	public AbstractEntitiesImporter(FileRepositoryCollectionFactory fileRepositoryCollectionFactory,
			EntityImportService entityImportService)
	{
		if (fileRepositoryCollectionFactory == null) throw new IllegalArgumentException(
				"fileRepositorySourceFactory is null");
		if (entityImportService == null) throw new IllegalArgumentException("entityImportService is null");
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.entityImportService = entityImportService;
	}

	@Override
	@Transactional(rollbackFor =
	{ IOException.class })
	public EntityImportReport importEntities(File file, DatabaseAction dbAction) throws IOException
	{
		FileRepositoryCollection collection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
		return importEntities(collection, dbAction);
	}

	@Override
	@Transactional(rollbackFor =
	{ IOException.class })
	public EntityImportReport importEntities(RepositoryCollection sourceRepositories, DatabaseAction dbAction)
			throws IOException
	{
		EntityImportReport importReport = new EntityImportReport();

		// import entities in order defined by entities map
		for (String entityName : getEntitiesImportable())
		{
			Repository repository = sourceRepositories.getRepository(entityName);
			if (repository != null)
			{
				try
				{
					int nr = entityImportService.importEntity(entityName, repository, dbAction);
					if (nr > 0)
					{
						importReport.addEntityCount(entityName, nr);
					}
				}
				catch (MolgenisValidationException e)
				{
					for (ConstraintViolation violation : e.getViolations())
					{
						if (null != violation.getRownr())
						{
							violation.setImportInfo(String.format("Sheet: '%s', row: %d", entityName,
									violation.getRownr() + 1));
						}
						else
						{
							violation.setImportInfo(String.format("Sheet: '%s'", entityName));
						}

					}

					throw e;
				}
			}
		}

		return importReport;
	}

	protected abstract Set<String> getEntitiesImportable();

}
