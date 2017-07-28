package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.oneclickimporter.exceptions.EmptyFileException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.service.Impl.CsvServiceImpl;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;
import static org.testng.Assert.assertEquals;

public class CsvServiceTest
{
	private CsvService csvService = new CsvServiceImpl();

	@Test
	public void buildLinesFromFileTest()
			throws InvalidFormatException, IOException, URISyntaxException, NoDataException, EmptyFileException
	{
		List<String> actual = csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/simple-valid.csv"));
		List<String> expected = newArrayList("name,superpower", "Mark,arrow functions", "Connor,Oldschool syntax",
				"Fleur,Lambda Magician", "Dennis,Root access");

		assertEquals(actual, expected);
	}

	@Test(expectedExceptions = EmptyFileException.class, expectedExceptionsMessageRegExp = "File \\[empty-file.csv\\] is empty")
	public void buildLinesWithEmptyFile()
			throws InvalidFormatException, IOException, URISyntaxException, NoDataException, EmptyFileException
	{
		csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/empty-file.csv"));
	}

	@Test(expectedExceptions = NoDataException.class, expectedExceptionsMessageRegExp = "Header was found, but no data is present in file \\[header-without-data.csv\\]")
	public void buildLinesWithHeaderOnly()
			throws InvalidFormatException, IOException, URISyntaxException, NoDataException, EmptyFileException
	{
		csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/header-without-data.csv"));
	}

	@Test
	public void testSplitLineOnSeparator()
	{
		String line = "\"hello, world\",\"25\",\"24,6\",\"FALSE\"";
		String[] actual = csvService.splitLineOnSeparator(line);
		String[] expected = { "\"hello, world\"", "\"25\"", "\"24,6\"", "\"FALSE\"" };

		assertEquals(actual, expected);
	}
}
