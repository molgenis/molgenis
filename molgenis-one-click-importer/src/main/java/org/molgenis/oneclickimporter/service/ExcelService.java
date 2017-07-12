package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.IOException;

/**
 * Creates a single excel sheet given a file, the file should contain a valid .xls or .xlsx file
 */
public interface ExcelService
{
	/**
	 * Builds a excel sheet based on a file.
	 *
	 * It is assumed that the file contains a single exel sheet
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	Sheet buildExcelSheetFromFile(final File file) throws IOException, InvalidFormatException;
}
