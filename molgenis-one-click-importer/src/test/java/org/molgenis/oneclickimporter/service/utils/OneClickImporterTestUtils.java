package org.molgenis.oneclickimporter.service.utils;

import com.google.common.io.Resources;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.service.CsvService;
import org.molgenis.oneclickimporter.service.ExcelService;
import org.molgenis.oneclickimporter.service.impl.CsvServiceImpl;
import org.molgenis.oneclickimporter.service.impl.ExcelServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class OneClickImporterTestUtils
{
	public static List<Sheet> loadSheetFromFile(Class<?> clazz, String fileName)
			throws IOException, InvalidFormatException, URISyntaxException, EmptySheetException
	{
		URL resourceUrl = Resources.getResource(clazz, fileName);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		ExcelService excelService = new ExcelServiceImpl();
		return excelService.buildExcelSheetsFromFile(file);
	}

	public static List<String[]> loadLinesFromFile(Class<?> clazz, String fileName)
			throws IOException, URISyntaxException
	{
		URL resourceUrl = Resources.getResource(clazz, fileName);
		File file = new File(new URI(resourceUrl.toString()).getPath());

		CsvService csvService = new CsvServiceImpl();
		return csvService.buildLinesFromFile(file);
	}

	public static File loadFile(Class<?> clazz, String fileName)
			throws IOException, InvalidFormatException, URISyntaxException
	{
		URL resourceUrl = Resources.getResource(clazz, fileName);
		return new File(new URI(resourceUrl.toString()).getPath());
	}
}
