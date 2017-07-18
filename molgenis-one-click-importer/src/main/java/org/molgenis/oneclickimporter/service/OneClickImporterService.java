package org.molgenis.oneclickimporter.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.DataCollection;

public interface OneClickImporterService
{
	/**
	 * Generate {@link DataCollection} with sheet contents
	 *
	 * @param dataCollectionName
	 * @param sheet
	 */
	DataCollection buildDataCollection(String dataCollectionName, Sheet sheet);
}
