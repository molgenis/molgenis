package org.molgenis.io.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.io.processor.CellProcessor;
import org.molgenis.util.tuple.Tuple;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelSheetReaderTest
{
	private ExcelReader excelReader;
	private ExcelSheetReader excelSheetReader;

	@BeforeMethod
	public void setUp() throws IOException
	{
		excelReader = new ExcelReader(this.getClass().getResourceAsStream("/test.xls"));
		excelSheetReader = excelReader.getSheet("test");
	}

	@AfterMethod
	public void tearDown() throws IOException
	{
		excelReader.close();
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelSheetReader()
	{
		new ExcelSheetReader(null, true, null);
	}

	@Test
	public void addCellProcessor_header()
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();
		excelSheetReader.addCellProcessor(processor);
		for (@SuppressWarnings("unused")
		Tuple tuple : excelSheetReader)
		{
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data()
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		excelSheetReader.addCellProcessor(processor);
		for (Tuple tuple : excelSheetReader)
			tuple.get("col2");
		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void colNamesIterator() throws IOException
	{
		Iterator<String> colNamesIt = excelSheetReader.colNamesIterator();
		assertTrue(colNamesIt.hasNext());
		assertEquals(colNamesIt.next(), "col1");
		assertTrue(colNamesIt.hasNext());
		assertEquals(colNamesIt.next(), "col2");
	}

	@Test
	public void getName()
	{
		assertEquals(excelSheetReader.getName(), "test");
	}

	@Test
	public void getNrRows()
	{
		assertEquals(excelSheetReader.getNrRows(), 5);
	}

	@Test
	public void hasColNames()
	{
		assertTrue(excelSheetReader.hasColNames());
	}

	@Test
	public void iterator()
	{
		Iterator<Tuple> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Tuple row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Tuple row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Tuple row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Tuple row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}

	@Test
	public void colNamesIteratorAndIterator() throws IOException
	{
		Iterator<String> colNamesIt = excelSheetReader.colNamesIterator();
		assertTrue(colNamesIt.hasNext());
		assertEquals(colNamesIt.next(), "col1");
		assertTrue(colNamesIt.hasNext());
		assertEquals(colNamesIt.next(), "col2");

		Iterator<Tuple> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Tuple row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Tuple row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Tuple row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Tuple row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}
}
