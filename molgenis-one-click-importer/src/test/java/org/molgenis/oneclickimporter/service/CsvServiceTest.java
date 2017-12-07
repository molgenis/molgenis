package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.MissingDataException;
import org.molgenis.oneclickimporter.service.impl.CsvServiceImpl;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;
import static org.testng.Assert.assertEquals;

public class CsvServiceTest
{
	private CsvService csvService = new CsvServiceImpl();

	@Test
	public void buildLinesFromFileTest()
			throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException
	{
		List<String[]> actual = csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/simple-valid.csv"));
		List<String[]> expected = new ArrayList<>();
		expected.add(new String[] { "name", "superpower" });
		expected.add(new String[] { "Mark", "arrow functions" });
		expected.add(new String[] { "Connor", "Oldschool syntax" });
		expected.add(new String[] { "Fleur", "Lambda Magician" });
		expected.add(new String[] { "Dennis", "Root access" });

		assertEquals(actual, expected);

	}

	@Test(expectedExceptions = EmptySheetException.class, expectedExceptionsMessageRegExp = "sheettype:CSV file fileName:empty-file.csv")
	public void buildLinesWithEmptyFile()
			throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException
	{
		csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/empty-file.csv"));
	}

	@Test(expectedExceptions = MissingDataException.class, expectedExceptionsMessageRegExp = "sheettype:CSV file filename:header-without-data.csv")
	public void buildLinesWithHeaderOnly()
			throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException
	{
		csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/header-without-data.csv"));
	}

}
