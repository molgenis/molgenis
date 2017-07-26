package org.molgenis.oneclickimporter.service;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.mockito.Mock;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.oneclickimporter.exceptions.EmptyFileException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.exceptions.NoDataException;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.Impl.OneClickImporterServiceImpl;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadLinesFromFile;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadSheetFromFile;
import static org.testng.Assert.*;

public class OneClickImporterServiceTest
{
	@Mock
	private CsvService csvService;
	private OneClickImporterService oneClickImporterService;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		oneClickImporterService = new OneClickImporterServiceImpl(csvService);
	}

	@Test
	public void testBuildDataCollectionWithSimpleValidExcelFile()
			throws IOException, InvalidFormatException, URISyntaxException, NoDataException, EmptySheetException
	{
		List<Sheet> sheets = loadSheetFromFile(OneClickImporterServiceTest.class, "/simple-valid.xlsx");
		DataCollection actual = oneClickImporterService.buildDataCollectionsFromExcel(sheets).get(0);

		Column c1 = Column.create("name", 0, newArrayList("Mark", "Connor", "Fleur", "Dennis"));
		Column c2 = Column.create("superpower", 1,
				newArrayList("arrow functions", "Oldschool syntax", "Lambda Magician", "Root access"));

		DataCollection expected = DataCollection.create("Sheet1", newArrayList(c1, c2));

		assertEquals(actual, expected);
	}

	@Test
	public void testBuildDataSheetWithValidFormulaFile()
			throws IOException, InvalidFormatException, URISyntaxException, NoDataException, EmptySheetException
	{
		List<Sheet> sheets = loadSheetFromFile(OneClickImporterServiceTest.class, "/valid-with-formula.xlsx");
		DataCollection actual = oneClickImporterService.buildDataCollectionsFromExcel(sheets).get(0);

		Column c1 = Column.create("name", 0, newArrayList("Mark", "Mariska"));
		Column c2 = Column.create("age", 1, newArrayList(26.0, 22.0));

		DataCollection expected = DataCollection.create("Sheet1", newArrayList(c1, c2));

		assertEquals(actual, expected);
	}

	@Test
	public void testBuildDataSheetBuildsColumnsOfEqualLength()
			throws IOException, InvalidFormatException, URISyntaxException, NoDataException, EmptySheetException
	{
		List<Sheet> sheets = loadSheetFromFile(OneClickImporterServiceTest.class, "/valid-with-blank-values.xlsx");
		DataCollection actual = oneClickImporterService.buildDataCollectionsFromExcel(sheets).get(0);

		Column c1 = Column.create("name", 0, newArrayList("Mark", "Bart", "Tommy", "Sido", "Connor", null));
		Column c2 = Column.create("favorite food", 1,
				newArrayList("Fries", null, "Vegan food", "Pizza", null, "Spinache"));

		DataCollection expected = DataCollection.create("Sheet1", newArrayList(c1, c2));
		assertEquals(actual, expected);

		assertEquals(6, c1.getDataValues().size());
		assertEquals(6, c2.getDataValues().size());
	}

	@Test
	public void testBuildDataSheetWithComplexFile()
			throws IOException, InvalidFormatException, URISyntaxException, NoDataException, EmptySheetException
	{
		List<Sheet> sheets = loadSheetFromFile(OneClickImporterServiceTest.class, "/complex-valid.xlsx");
		DataCollection actual = oneClickImporterService.buildDataCollectionsFromExcel(sheets).get(0);

		Column c1 = Column.create("first name", 0,
				newArrayList("Mark", "Fleur", "Dennis", "Bart", "Sido", "Mariska", "Tommy", "Connor", "Piet", "Jan"));
		Column c2 = Column.create("last name", 1,
				newArrayList("de Haan", "Kelpin", "Hendriksen", "Charbon", "Haakma", "Slofstra", "de Boer",
						"Stroomberg", "Klaassen", null));
		Column c3 = Column.create("full name", 2,
				newArrayList("Mark de Haan", "Fleur Kelpin", "Dennis Hendriksen", "Bart Charbon", "Sido Haakma",
						"Mariska Slofstra", "Tommy de Boer", "Connor Stroomberg", "Piet Klaassen", null));
		Column c4 = Column.create("UMCG employee", 3,
				newArrayList(true, true, true, true, true, true, true, true, false, false));
		Column c5 = Column.create("Age", 4, newArrayList(26.0, null, null, null, null, 22.0, 27.0, null, 53.0, 32.0));

		DataCollection expected = DataCollection.create("Sheet1", newArrayList(c1, c2, c3, c4, c5));
		assertEquals(actual, expected);
	}

	@Test
	public void testBuildDataSheetWithDates()
			throws IOException, InvalidFormatException, URISyntaxException, NoDataException, EmptySheetException
	{
		List<Sheet> sheets = loadSheetFromFile(OneClickImporterServiceTest.class, "/valid-with-dates.xlsx");
		DataCollection actual = oneClickImporterService.buildDataCollectionsFromExcel(sheets).get(0);

		Column c1 = Column.create("dates", 0,
				newArrayList("2018-01-03T00:00", "2018-01-04T00:00", "2018-01-05T00:00", "2018-01-06T00:00",
						"2018-01-07T00:00"));

		Column c2 = Column.create("event", 1,
				newArrayList("being cool day", "bike day", "sleep day", "bye bye day", "work day"));

		DataCollection expected = DataCollection.create("Sheet1", newArrayList(c1, c2));
		assertEquals(actual, expected);
	}

	@Test
	public void testBuildDataCollectionWithSimpleValidCsvFile()
			throws IOException, URISyntaxException, NoDataException, EmptyFileException
	{
		String[] header = { "name", "superpower" };
		when(csvService.splitLineOnSeparator("name,superpower")).thenReturn(header);

		String[] line1 = { "Mark", "arrow functions" };
		when(csvService.splitLineOnSeparator("Mark,arrow functions")).thenReturn(line1);

		String[] line2 = { "Connor", "Oldschool syntax" };
		when(csvService.splitLineOnSeparator("Connor,Oldschool syntax")).thenReturn(line2);

		String[] line3 = { "Fleur", "Lambda Magician" };
		when(csvService.splitLineOnSeparator("Fleur,Lambda Magician")).thenReturn(line3);

		String[] line4 = { "Dennis", "Root access" };
		when(csvService.splitLineOnSeparator("Dennis,Root access")).thenReturn(line4);

		oneClickImporterService = new OneClickImporterServiceImpl(csvService);

		List<String> lines = loadLinesFromFile(OneClickImporterServiceTest.class, "/simple-valid.csv");
		DataCollection actual = oneClickImporterService.buildDataCollectionFromCsv("simple-valid", lines);

		Column c1 = Column.create("name", 0, newArrayList("Mark", "Connor", "Fleur", "Dennis"));
		Column c2 = Column.create("superpower", 1,
				newArrayList("arrow functions", "Oldschool syntax", "Lambda Magician", "Root access"));

		DataCollection expected = DataCollection.create("simple-valid", newArrayList(c1, c2));
		assertEquals(actual, expected);
	}

	@Test
	public void testBuildDataCollectionWithComplexValidCsvFile()
			throws IOException, URISyntaxException, NoDataException, EmptyFileException
	{
		String[] header = { "first name", "last name", "full name", "UMCG employee", "Age" };
		when(csvService.splitLineOnSeparator("first name,last name,full name,UMCG employee,Age")).thenReturn(header);

		String[] line1 = { "Mark", "de Haan", "Mark de Haan", "TRUE", "26" };
		when(csvService.splitLineOnSeparator("Mark,de Haan,Mark de Haan,TRUE,26")).thenReturn(line1);

		String[] line2 = { "Fleur", "Kelpin", "Fleur Kelpin", "TRUE", "" };
		when(csvService.splitLineOnSeparator("Fleur,Kelpin,Fleur Kelpin,TRUE,")).thenReturn(line2);

		String[] line3 = { "Dennis", "Hendriksen", "Dennis Hendriksen", "TRUE", "" };
		when(csvService.splitLineOnSeparator("Dennis,Hendriksen,Dennis Hendriksen,TRUE,")).thenReturn(line3);

		String[] line4 = { "Bart", "Charbon", "Bart Charbon", "TRUE", "" };
		when(csvService.splitLineOnSeparator("Bart,Charbon,Bart Charbon,TRUE,")).thenReturn(line4);

		String[] line5 = { "Sido", "Haakma", "Sido Haakma", "TRUE", "" };
		when(csvService.splitLineOnSeparator("Sido,Haakma,Sido Haakma,TRUE,")).thenReturn(line5);

		String[] line6 = { "Mariska", "Slofstra", "Mariska Slofstra", "TRUE", "22" };
		when(csvService.splitLineOnSeparator("Mariska,Slofstra,Mariska Slofstra,TRUE,22")).thenReturn(line6);

		String[] line7 = { "Tommy", "de Boer", "Tommy de Boer", "TRUE", "27" };
		when(csvService.splitLineOnSeparator("Tommy,de Boer,Tommy de Boer,TRUE,27")).thenReturn(line7);

		String[] line8 = { "Connor", "Stroomberg", "Connor Stroomberg", "TRUE", "" };
		when(csvService.splitLineOnSeparator("Connor,Stroomberg,Connor Stroomberg,TRUE,")).thenReturn(line8);

		String[] line9 = { "Piet", "Klaassen", "Piet Klaassen", "FALSE", "53" };
		when(csvService.splitLineOnSeparator("Piet,Klaassen,Piet Klaassen,FALSE,53")).thenReturn(line9);

		String[] line10 = { "Jan", "", "", "FALSE", "32" };
		when(csvService.splitLineOnSeparator("Jan,,,FALSE,32")).thenReturn(line10);

		oneClickImporterService = new OneClickImporterServiceImpl(csvService);

		List<String> lines = loadLinesFromFile(OneClickImporterServiceTest.class, "/complex-valid.csv");
		DataCollection actual = oneClickImporterService.buildDataCollectionFromCsv("complex-valid", lines);

		Column c1 = Column.create("first name", 0,
				newArrayList("Mark", "Fleur", "Dennis", "Bart", "Sido", "Mariska", "Tommy", "Connor", "Piet", "Jan"));
		Column c2 = Column.create("last name", 1,
				newArrayList("de Haan", "Kelpin", "Hendriksen", "Charbon", "Haakma", "Slofstra", "de Boer",
						"Stroomberg", "Klaassen", null));
		Column c3 = Column.create("full name", 2,
				newArrayList("Mark de Haan", "Fleur Kelpin", "Dennis Hendriksen", "Bart Charbon", "Sido Haakma",
						"Mariska Slofstra", "Tommy de Boer", "Connor Stroomberg", "Piet Klaassen", null));
		Column c4 = Column.create("UMCG employee", 3,
				newArrayList(true, true, true, true, true, true, true, true, false, false));
		Column c5 = Column.create("Age", 4, newArrayList(26, null, null, null, null, 22, 27, null, 53, 32));

		DataCollection expected = DataCollection.create("complex-valid", newArrayList(c1, c2, c3, c4, c5));
		assertEquals(actual, expected);
	}

	@Test
	public void testIsFirstColumnUnique()
	{
		Column column = Column.create("col", 0, Arrays.asList(1, 2, 3));
		assertTrue(oneClickImporterService.hasUniqueValues(column), "should return true for unique int list");

		column = Column.create("col", 0, Arrays.asList("a", "b", "c"));
		assertTrue(oneClickImporterService.hasUniqueValues(column), "should return true for unique string list");

		column = Column.create("col", 0, Arrays.asList(1, 2, 1));
		assertFalse(oneClickImporterService.hasUniqueValues(column), "should return false for non-unique int list");

		column = Column.create("col", 0, Arrays.asList(1, null, 2));
		assertFalse(oneClickImporterService.hasUniqueValues(column), "should return false a list containing null's ");

		column = Column.create("col", 0, Arrays.asList(1, "1"));
		assertFalse(oneClickImporterService.hasUniqueValues(column), "should return false if types differ ");
	}

	@Test
	public void testCastType()
	{
		Object value = 1;
		AttributeType type = INT;
		Object casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Integer);

		value = 1.0;
		type = INT;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Integer);

		value = "1";
		type = INT;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Integer);

		value = "1";
		type = STRING;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof String);

		Long myLong = (long) Integer.MAX_VALUE + 1;
		value = myLong;
		type = LONG;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Long);

		Double myDouble = (double) Long.MAX_VALUE + 1;
		value = myDouble;
		type = DECIMAL;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Double);

		value = "1.1";
		type = DECIMAL;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof Double);

		value = 1.1;
		type = STRING;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof String);

		value = 1L;
		type = STRING;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof String);

		value = 1.1D;
		type = STRING;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof String);

		value = true;
		type = STRING;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof String);

		value = "2018-01-03T00:00";
		type = DATE;
		casted = oneClickImporterService.castValueAsAttributeType(value, type);
		assertTrue(casted instanceof LocalDate);
	}
}
