package org.molgenis.omx.importer;

import java.io.IOException;
import java.util.Map;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntitySource;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.omx.converters.ValueConverterException;

/**
 * Import OMX data from file (e.g. csv, tsv, xls, xlsx)
 * 
 * @author Roan
 */
public interface OmxImporterService
{
	/**
	 * Import OMX data from file
	 * 
	 * @param repository
	 * @param dataImportableMap
	 *            map containing for each entity (e.g. xls worksheet name, csv file name) whether or not to import
	 * @param entityAction
	 * @return report containing import information
	 * @throws IOException
	 * @throws ValueConverterException
	 */
	EntityImportReport doImport(EntitySource entitySource, Map<String, Boolean> dataImportableMap,
			DatabaseAction databaseAction) throws IOException, ValueConverterException;
}
