package org.molgenis.dataexplorer.download;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataRequest;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DataExplorerDownloadHandlerTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;
	@Mock
	private AttributeFactory attributeFactory;
	@Mock
	private EntityType entityType;
	@Mock
	private Attribute attribute1;
	@Mock
	private Attribute attribute2;
	@Mock
	private Attribute attribute3;
	@Mock
	private QueryImpl<Entity> query;
	@Mock
	private Entity entity1;
	@Mock
	private Entity entity2;
	@Mock
	private Entity refEntity1;
	@Mock
	private Entity refEntity2;

	private DataExplorerDownloadHandler dataExplorerDownloadHandler;

	@BeforeMethod
	public void beforeTest() throws IOException
	{
		dataExplorerDownloadHandler = new DataExplorerDownloadHandler(dataService, attributeFactory);
	}

	@Test(dataProvider = "writeToExcelDataProvider")
	public void testWriteToCSV(DataRequest.ColNames colNames, DataRequest.EntityValues entityValues,
			Map<String, List<List<String>>> expected) throws Exception
	{
		String entityTypeId = "sys_set_thousandgenomes";
		when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(attribute1, attribute2, attribute3));
		if (colNames == DataRequest.ColNames.ATTRIBUTE_LABELS)
		{
			when(attribute1.getLabel()).thenReturn("attr1Label");
			when(attribute2.getLabel()).thenReturn("attr2Label");
		}
		when(attribute1.getName()).thenReturn("attr1");
		when(attribute2.getName()).thenReturn("attr2");
		when(attribute3.getName()).thenReturn("attr3");

		DataRequest dataRequest = new DataRequest();
		dataRequest.setEntityName(entityTypeId);
		dataRequest.setQuery(query);
		dataRequest.setAttributeNames(asList("attr1", "attr2"));
		dataRequest.setColNames(colNames);
		dataRequest.setEntityValues(entityValues);

		when(dataService.findAll(entityTypeId, query)).thenReturn(Stream.of(entity1, entity2));
		doReturn("entity1attr1").when(entity1).get("attr1");
		doReturn(refEntity1).when(entity1).get("attr2");
		doReturn("entity2attr1").when(entity2).get("attr1");
		doReturn(refEntity2).when(entity2).get("attr2");

		if (entityValues == DataRequest.EntityValues.ENTITY_LABELS)
		{
			when(refEntity1.getLabelValue()).thenReturn("refEntity1Label");
			when(refEntity2.getLabelValue()).thenReturn("refEntity2Label");
		}
		else
		{
			when(refEntity1.getIdValue()).thenReturn("refEntity1Id");
			when(refEntity2.getIdValue()).thenReturn("refEntity2Id");
		}

		File tmpFile = File.createTempFile("download", ".csv");
		FileOutputStream fos = new FileOutputStream(tmpFile);
		dataExplorerDownloadHandler.writeToCsv(dataRequest, fos, ',');
		assertEquals(readCsv(tmpFile), expected.get(entityTypeId), "entities should get exported");
		assertTrue(tmpFile.delete());
		verifyNoMoreInteractions(refEntity1, refEntity2, attribute1, attribute2);
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Total number of cells for this download exceeds the maximum of 500000 for .xlsx downloads, please use .csv instead")
	public void testWriteToExcelTooManyCells() throws Exception
	{
		when(dataService.count("sys_set_thousandgenomes", query)).thenReturn(2500001L);
		when(dataService.getEntityType("sys_set_thousandgenomes")).thenReturn(entityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(attribute1, attribute2, attribute3));
		when(attribute1.getName()).thenReturn("attr1");
		when(attribute2.getName()).thenReturn("attr2");
		when(attribute3.getName()).thenReturn("attr3");

		DataRequest dataRequest = new DataRequest();
		dataRequest.setEntityName("sys_set_thousandgenomes");
		dataRequest.setQuery(query);
		dataRequest.setAttributeNames(asList("attr1", "attr2"));
		dataRequest.setColNames(DataRequest.ColNames.ATTRIBUTE_NAMES);
		dataRequest.setEntityValues(DataRequest.EntityValues.ENTITY_LABELS);

		dataExplorerDownloadHandler.writeToExcel(dataRequest, mock(OutputStream.class));
	}

	@DataProvider
	public static Object[][] writeToExcelDataProvider()
	{
		return new Object[][] {
				new Object[] { DataRequest.ColNames.ATTRIBUTE_NAMES, DataRequest.EntityValues.ENTITY_LABELS,
						ImmutableMap.of("sys_set_thousandgenomes",
								asList(asList("attr1", "attr2"), asList("entity1attr1", "refEntity1Label"),
										asList("entity2attr1", "refEntity2Label"))) },
				new Object[] { DataRequest.ColNames.ATTRIBUTE_LABELS, DataRequest.EntityValues.ENTITY_IDS,
						ImmutableMap.of("sys_set_thousandgenomes",
								asList(asList("attr1Label", "attr2Label"), asList("entity1attr1", "refEntity1Id"),
										asList("entity2attr1", "refEntity2Id"))) },
				new Object[] { DataRequest.ColNames.ATTRIBUTE_NAMES, DataRequest.EntityValues.ENTITY_IDS,
						ImmutableMap.of("sys_set_thousandgenomes",
								asList(asList("attr1", "attr2"), asList("entity1attr1", "refEntity1Id"),
										asList("entity2attr1", "refEntity2Id"))) },
				new Object[] { DataRequest.ColNames.ATTRIBUTE_LABELS, DataRequest.EntityValues.ENTITY_LABELS,
						ImmutableMap.of("sys_set_thousandgenomes",
								asList(asList("attr1Label", "attr2Label"), asList("entity1attr1", "refEntity1Label"),
										asList("entity2attr1", "refEntity2Label"))) } };
	}

	@Test(dataProvider = "writeToExcelDataProvider")
	public void testWriteToExcel(DataRequest.ColNames colNames, DataRequest.EntityValues entityValues,
			Map<String, List<List<String>>> expected) throws Exception
	{
		String entityTypeId = "sys_set_thousandgenomes";
		when(dataService.count(entityTypeId, query)).thenReturn(2L);
		when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(attribute1, attribute2, attribute3));
		if (colNames == DataRequest.ColNames.ATTRIBUTE_LABELS)
		{
			when(attribute1.getLabel()).thenReturn("attr1Label");
			when(attribute2.getLabel()).thenReturn("attr2Label");
		}
		when(attribute1.getName()).thenReturn("attr1");
		when(attribute2.getName()).thenReturn("attr2");
		when(attribute3.getName()).thenReturn("attr3");

		DataRequest dataRequest = new DataRequest();
		dataRequest.setEntityName(entityTypeId);
		dataRequest.setQuery(query);
		dataRequest.setAttributeNames(asList("attr1", "attr2"));
		dataRequest.setColNames(colNames);
		dataRequest.setEntityValues(entityValues);

		when(dataService.findAll(entityTypeId, query)).thenReturn(Stream.of(entity1, entity2));
		doReturn("entity1attr1").when(entity1).get("attr1");
		doReturn(refEntity1).when(entity1).get("attr2");
		doReturn("entity2attr1").when(entity2).get("attr1");
		doReturn(refEntity2).when(entity2).get("attr2");

		if (entityValues == DataRequest.EntityValues.ENTITY_LABELS)
		{
			when(refEntity1.getLabelValue()).thenReturn("refEntity1Label");
			when(refEntity2.getLabelValue()).thenReturn("refEntity2Label");
		}
		else
		{
			when(refEntity1.getIdValue()).thenReturn("refEntity1Id");
			when(refEntity2.getIdValue()).thenReturn("refEntity2Id");
		}

		File tmpFile = File.createTempFile("download", ".xlsx");
		FileOutputStream fos = new FileOutputStream(tmpFile);
		dataExplorerDownloadHandler.writeToExcel(dataRequest, fos);
		assertEquals(readExcel(tmpFile), expected, "entities should get exported");
		assertTrue(tmpFile.delete());
		verifyNoMoreInteractions(refEntity1, refEntity2, attribute1, attribute2);
	}

	private Map<String, List<List<String>>> readExcel(File tmpFile) throws IOException, InvalidFormatException
	{
		Map<String, List<List<String>>> actual = newHashMap();
		try (Workbook workbook = WorkbookFactory.create(tmpFile))
		{
			List<List<String>> sheetResult = newArrayList();
			for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++)
			{
				Sheet sheet = workbook.getSheetAt(sheetNum);
				for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++)
				{
					List<String> rowResult = newArrayList();
					Row row = sheet.getRow(rowNum);
					for (int colNum = 0; colNum < row.getLastCellNum(); colNum++)
					{
						rowResult.add(row.getCell(colNum, CREATE_NULL_AS_BLANK).getStringCellValue());
					}
					sheetResult.add(rowResult);
				}
				actual.put(sheet.getSheetName(), sheetResult);
			}
		}
		return actual;
	}

	private List<List<String>> readCsv(File file) throws IOException, InvalidFormatException
	{
		try (CSVReader reader = new CSVReader(
				new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"))))
		{
			return reader.readAll().stream().map(Arrays::asList).collect(Collectors.toList());
		}
	}
}