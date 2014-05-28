package org.molgenis.omx.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.EmxImportServiceImpl;
import org.molgenis.data.importer.EmxImporterService;
import org.molgenis.data.importer.EntitiesValidationReportImpl;
import org.molgenis.data.vcf.VcfRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntitiesValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by charbonb on 22/05/14.
 */
@Component
public class EntityImporterValidator {

    @Autowired
    EntitiesValidator validator;
    @Autowired
    private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

    EntitiesValidationReport validate(File file, DatabaseAction action) throws IOException {
        EntitiesValidationReport validationReport = new EntitiesValidationReportImpl();

        RepositoryCollection repositoryCollection = fileRepositoryCollectionFactory
                .createFileRepositoryCollection(file);
        if (repositoryCollection instanceof VcfRepositoryCollection)
        {
            // TODO: validate VCF?
            validationReport.getSheetsImportable().put(StringUtils.stripFilenameExtension(file.getName()), true);
        }
        // MySQLRepository
        else if (repositoryCollection.getRepositoryByEntityName("attributes") != null)
        {
            EmxImporterService importer = new EmxImportServiceImpl();
            validationReport = importer.validateImport(repositoryCollection);
        }
        // JPARepository
        else
        {
            validationReport = validator.validate(file);
        }
        return validationReport;
    }
}
