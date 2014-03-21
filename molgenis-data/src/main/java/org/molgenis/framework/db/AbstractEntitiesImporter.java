package org.molgenis.framework.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositorySourceFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.importer.EntityImportService;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractEntitiesImporter implements EntitiesImporter
{
	private final FileRepositorySourceFactory fileRepositorySourceFactory;
	private final EntityImportService entityImportService;

	public AbstractEntitiesImporter(FileRepositorySourceFactory fileRepositorySourceFactory,
			EntityImportService entityImportService)
	{
		if (fileRepositorySourceFactory == null) throw new IllegalArgumentException(
				"fileRepositorySourceFactory is null");
		if (entityImportService == null) throw new IllegalArgumentException("entityImportService is null");
		this.fileRepositorySourceFactory = fileRepositorySourceFactory;
		this.entityImportService = entityImportService;
	}

	@Override
	@Transactional(rollbackFor =
	{ IOException.class })
	public EntityImportReport importEntities(File file, DatabaseAction dbAction) throws IOException
	{
		return importEntities(fileRepositorySourceFactory.createFileRepositorySource(file).getRepositories(), dbAction);
	}

	@Override
	@Transactional(rollbackFor =
	{ IOException.class })
	public EntityImportReport importEntities(List<Repository> sourceRepositories, DatabaseAction dbAction)
			throws IOException
	{
		EntityImportReport importReport = new EntityImportReport();

		// map entity names on repositories
		Map<String, Repository> repositoryMap = new HashMap<String, Repository>();
		for (Repository repository : sourceRepositories)
		{
			repositoryMap.put(repository.getName().toLowerCase(), repository);
		}

		// import entities in order defined by entities map
		for (String entityName : getEntitiesImportable())
		{
			Repository repository = repositoryMap.get(entityName);
			if (repository != null)
			{
				try
				{
					int nr = entityImportService.importEntity(entityName, repository, dbAction);
					if (nr > 0)
					{
						importReport.addEntityCount(entityName, nr);
						importReport.addNrImported(nr);
					}
				}
				catch (MolgenisValidationException e)
				{
					for (ConstraintViolation violation : e.getViolations())
					{
						if (violation.getRownr() > 0)
						{
							// Rownr +1 for header
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
