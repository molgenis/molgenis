package org.molgenis.oneclickimporter.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.DataCollection;

public interface OneClickImporterService
{
	/**
	 * Generate {@link DataCollection} with sheet contents
	 *
	 * @param sheet
	 */
	DataCollection buildDataCollection(Sheet sheet);

	// void buildDataCollection(String csvContent);
}
