package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Repository;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.converters.ValueConverterException;

/**
 * Import OMX data from file (e.g. csv, tsv, xls, xlsx)
 * 
 * @author Roan
 */
public interface OmxImporterService
{
	public static final String DATASET_SHEET_PREFIX = "dataset_";

	/**
	 * Import OMX data from file
	 * 
	 * @param repository
	 * @param entityAction
	 * @return report containing import information
	 * @throws IOException
	 * @throws ValueConverterException
	 */
	EntityImportReport doImport(List<Repository> repositories, DatabaseAction databaseAction) throws IOException,
			ValueConverterException;
}
