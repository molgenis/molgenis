package org.molgenis.omx.importer;

import org.molgenis.data.*;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.framework.db.EntityImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;

/**
 * Created by charbonb on 22/05/14.
 */
@Component
public class EntityImporterController
{
    FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
    EntityImporterFactory entityImporterFactory;

    @Autowired
    public EntityImporterController(EntityImporterFactory entityImporterFactory, FileRepositoryCollectionFactory fileRepositoryCollectionFactory){
        this.entityImporterFactory = entityImporterFactory;
        this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
    }

    public ValidationReport handleImportRequest(File file, DatabaseAction dba)
	{
        FileRepositoryCollection collection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);
        EntityImporterValidator validator = new EntityImporterValidator();
        EntityImporterDependencyResolver dependencyResolver = new EntityImporterDependencyResolver();

		ValidationReport report = validator.validate(collection, dba);
		if (report.isValid())
		{
			dependencyResolver.sort(collection);
			for (String inEntityName : collection.getEntityNames())
			{
				Repository inRepository = collection.getRepositoryByEntityName(inEntityName);

				CrudRepository outRepository = (CrudRepository) entityImporterFactory.getOutRepository(inRepository);

				if (dba == DatabaseAction.ADD) outRepository.add(inRepository);
				if (dba == DatabaseAction.UPDATE) outRepository.update(inRepository);
			}
		}
		return report;
	}
}
