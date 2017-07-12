package org.molgenis.oneclickimporter.service;

import com.google.common.io.Resources;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.oneclickimporter.service.Impl.ExcelServiceImpl;
import org.molgenis.oneclickimporter.service.Impl.OneClickImporterServiceImpl;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class OneClickImporterServiceTest
{
	private OneClickImporterService oneClickImporterService = new OneClickImporterServiceImpl();

	@Test
	public void testBuildDataSheet() throws URISyntaxException, IOException, InvalidFormatException
	{
		URL resourceUrl = Resources.getResource(OneClickImporterServiceTest.class, "/simple-valid.xlsx");
		File file = new File(new URI(resourceUrl.toString()).getPath());

		Sheet sheet = loadTestFile(file);
		oneClickImporterService.buildDataCollection(sheet);
	}

	private Sheet loadTestFile(File file) throws IOException, InvalidFormatException
	{
		ExcelService excelService = new ExcelServiceImpl();
		return excelService.buildExcelSheetFromFile(file);
	}
}
