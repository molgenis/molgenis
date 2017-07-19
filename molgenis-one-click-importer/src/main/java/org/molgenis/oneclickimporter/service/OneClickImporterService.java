package org.molgenis.oneclickimporter.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.DataCollection;

import java.util.List;

public interface OneClickImporterService
{
	/**
	 * Generate {@link DataCollection} from an Excel sheet
	 *
	 * @param dataCollectionName
	 * @param sheet
	 */
	DataCollection buildDataCollection(String dataCollectionName, Sheet sheet);

	/**
	 * Generate {@link DataCollection} from a List of file lines
	 *
	 * @param dataCollectionName
	 * @param lines
	 */
	DataCollection buildDataCollection(String dataCollectionName, List<String> lines);
}
