package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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
	CsvService csvService = new CsvServiceImpl();

	@Test
	public void buildLinesFromFileTest() throws InvalidFormatException, IOException, URISyntaxException
	{
		List<String> actual = csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/simple-valid.csv"));
		List<String> expected = newArrayList("name,superpower", "Mark,arrow functions", "Connor,Oldschool syntax",
				"Fleur,Lambda Magician", "Dennis,Root access");

		assertEquals(actual, expected);
	}
}
