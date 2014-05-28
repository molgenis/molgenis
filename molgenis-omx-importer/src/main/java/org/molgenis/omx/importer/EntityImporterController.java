package org.molgenis.omx.importer;

import org.molgenis.data.*;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;

/**
 * Created by charbonb on 22/05/14.
 */
@Component
public class EntityImporterController
{
    private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
    private EntityImporterFactory entityImporterFactory;
    private EntityImporterValidator validator;

    @Autowired
    public EntityImporterController(EntityImporterFactory entityImporterFactory, FileRepositoryCollectionFactory fileRepositoryCollectionFactory, EntityImporterValidator validator){
        this.entityImporterFactory = entityImporterFactory;
        this.fileRepositoryCollectionFactory = fileRepositoryCollectionFactory;
        this.validator = validator;
    }

    public EntitiesValidationReport handleImportRequest(File file, DatabaseAction dba) throws IOException {
        FileRepositoryCollection collection = fileRepositoryCollectionFactory.createFileRepositoryCollection(file);

        EntityImporterDependencyResolver dependencyResolver = new EntityImporterDependencyResolver();

        EntitiesValidationReport report = validator.validate(file, dba);
		if (!report.getSheetsImportable().containsValue(false))
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
