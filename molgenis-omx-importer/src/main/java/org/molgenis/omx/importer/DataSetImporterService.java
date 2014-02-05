package org.molgenis.omx.importer;

import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.EntityImportReport;

public interface DataSetImporterService
{
	EntityImportReport importDataSet(EntitySource entitySource, List<String> dataSetEntityNames,
			DatabaseAction databaseAction);

	EntityImportReport importSheet(Repository repo, String sheetName);
}