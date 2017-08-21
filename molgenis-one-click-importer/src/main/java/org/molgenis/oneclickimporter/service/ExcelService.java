package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Creates a list of {@link Sheet}s from an excel file
 */
public interface ExcelService
{
	/**
	 * Builds one or more excel sheets based on a xls or xlsx file
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	List<Sheet> buildExcelSheetsFromFile(final File file)
			throws IOException, InvalidFormatException, EmptySheetException;
}
