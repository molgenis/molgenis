package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charbonb on 22/05/14.
 */
@Component
public class EntityImporterController
{
	private final FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private final EntityImporterFactory entityImporterFactory;
	private final EntityImporterValidator validator;
	private final DataService dataService;

	@Autowired
	public EntityImporterController(EntityImporterFactory entityImporterFactory,
			FileRepositoryCollectionFactory fileRepositoryCollectionFactory, EntityImporterValidator validator,
			DataService dataService)
	{
		this.entityImporterFactory = entityImporterFactory;
		this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
		this.validator = validator;
		this.dataService = dataService;
	}

	public EntitiesValidationReport handleImportRequest(File file, DatabaseAction dba) throws IOException
	{
		FileRepositoryCollection collection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);

		EntityImporterDependencyResolver dependencyResolver = new EntityImporterDependencyResolver();

		EntitiesValidationReport report = validator.validate(file, dba);
		if (!report.getSheetsImportable().containsValue(false))
		{
			dependencyResolver.sort(collection);
			for (String entityName : collection.getEntityNames())
			{
				Repository inRepository = collection.getRepositoryByEntityName(entityName);

				CrudRepository outRepository = (CrudRepository) entityImporterFactory.getOutRepository(inRepository);

				if (dba == DatabaseAction.ADD) outRepository.add(inRepository);
				if (dba == DatabaseAction.UPDATE) outRepository.update(inRepository);

				if (!dataService.hasRepository(entityName))
				{
					dataService.addRepository(outRepository);
				}
			}
		}
		return report;
	}
}
