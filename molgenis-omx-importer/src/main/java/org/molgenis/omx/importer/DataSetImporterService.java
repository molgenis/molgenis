package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.converters.ValueConverterException;

public interface DataSetImporterService
{
	EntityImportReport importDataSet(EntitySource entitySource, List<String> dataSetEntityNames,
			DatabaseAction databaseAction) throws IOException, ValueConverterException;

	EntityImportReport importSheet(Repository repo, String sheetName) throws IOException, ValueConverterException;
}