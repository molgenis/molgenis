package org.molgenis.oneclickimporter.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.model.Column;

public interface OneClickImporterService
{
	/**
	 * Generate {@link Column} with sheet contents
	 *
	 * @param sheet
	 */
	void buildDataSheet(Sheet sheet);

	// void buildDataSheet(String csvContent);
}
