package org.molgenis.omx.importer;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;

/**
 * Created by charbonb on 22/05/14.
 */
public class EntityImporterValidator {

    ValidationReport validate(RepositoryCollection collection, DatabaseAction action){
         return new ValidationReport();
    }
}
