package org.molgenis.omx.importer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.omx.converters.ValueConverterException;

public interface DataSetImporterService
{
	void importDataSet(File file, List<String> dataSetEntityNames) throws IOException, ValueConverterException;

	void importSheet(Repository<? extends Entity> repo, String sheetName) throws IOException, ValueConverterException;
}