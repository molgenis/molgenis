package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.MissingDataException;
import org.molgenis.oneclickimporter.service.impl.ExcelServiceImpl;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;

public class ExcelServiceTest
{
	private ExcelService excelService = new ExcelServiceImpl();

	@Test(expectedExceptions = EmptySheetException.class, expectedExceptionsMessageRegExp = "sheettype:Excel sheet fileName:empty_sheet")
	public void buildExcelSheetsWithEmptyFile()
			throws InvalidFormatException, IOException, URISyntaxException, EmptySheetException
	{
		excelService.buildExcelSheetsFromFile(loadFile(ExcelServiceTest.class, "/empty-sheet.xlsx"));
	}

	@Test(expectedExceptions = MissingDataException.class, expectedExceptionsMessageRegExp = "sheettype:Excel sheet filename:Sheet1")
	public void buildExcelSheetsWithHeaderOnly()
			throws InvalidFormatException, IOException, URISyntaxException, EmptySheetException
	{
		excelService.buildExcelSheetsFromFile(loadFile(ExcelServiceTest.class, "/header-without-data.xlsx"));
	}
}