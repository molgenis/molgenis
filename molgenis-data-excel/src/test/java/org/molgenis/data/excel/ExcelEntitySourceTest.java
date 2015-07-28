package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.io.processor.CellProcessor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelEntitySourceTest
{
	private ExcelEntitySource excelEntitySource;

	@BeforeMethod
	public void beforeMethod()
	{
		excelEntitySource = new ExcelEntitySource(this.getClass().getResourceAsStream("/test.xls"), "excel://test",
				null);
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		excelEntitySource.close();
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelReaderIllegalUrl() throws IOException
	{
		new ExcelEntitySource("test", null);
	}

	@Test
	public void addCellProcessor_header() throws IOException
	{
		CellProcessor processor = mock(CellProcessor.class);
		when(processor.processHeader()).thenReturn(true);
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		excelEntitySource.addCellProcessor(processor);

		for (String sheetName : excelEntitySource.getEntityNames())
		{
			for (@SuppressWarnings("unused")
			Entity entity : excelEntitySource.getRepositoryByEntityName(sheetName))
			{
			}
		}

		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		excelEntitySource.addCellProcessor(processor);

		for (String sheetName : excelEntitySource.getEntityNames())
		{
			for (Entity entity : excelEntitySource.getRepositoryByEntityName(sheetName))
			{
				entity.get("col2");
			}
		}

		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void getSheetByIndex() throws IOException
	{
		assertNotNull(excelEntitySource.getSheet(0));
		assertNotNull(excelEntitySource.getSheet(1));
	}

	@Test
	public void getSheetByName() throws IOException
	{
		assertNotNull(excelEntitySource.getSheet("test"));
		assertNotNull(excelEntitySource.getSheet("Blad2"));
		assertNull(excelEntitySource.getSheet("doesnotexist"));
	}

	@Test
	public void getEntityNames()
	{
		Iterable<String> entityNames = excelEntitySource.getEntityNames();
		assertNotNull(entityNames);
		Iterator<String> it = entityNames.iterator();
		assertNotNull(it);
		assertTrue(it.hasNext());
		assertEquals(it.next(), "test");
		assertTrue(it.hasNext());
		assertEquals(it.next(), "Blad2");
	}

	@Test
	public void getNumberOfSheets()
	{
		assertEquals(excelEntitySource.getNumberOfSheets(), 2);
	}

	@Test
	public void getRepositoryByEntityName()
	{
		assertNotNull(excelEntitySource.getRepositoryByEntityName("test"));
		assertNotNull(excelEntitySource.getRepositoryByEntityName("Blad2"));
	}

	@Test
	public void getSheetName()
	{
		assertEquals(excelEntitySource.getSheetName(0), "test");
		assertEquals(excelEntitySource.getSheetName(1), "Blad2");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void getNonExistingSheetName()
	{
		excelEntitySource.getSheetName(2);
	}

	@Test
	public void getUrl()
	{
		assertNotNull(excelEntitySource.getUrl());
		System.out.println(excelEntitySource.getUrl());
		assertEquals(excelEntitySource.getUrl(), "excel://test");
	}

}
