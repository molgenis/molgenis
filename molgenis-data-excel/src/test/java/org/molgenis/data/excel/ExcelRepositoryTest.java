package org.molgenis.data.excel;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ExcelRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	private ExcelRepository excelSheetReader;

	private Workbook workbook;
	private InputStream is;

	@BeforeMethod
	public void beforeMethod() throws InvalidFormatException, IOException
	{
		is = getClass().getResourceAsStream("/test.xls");
		workbook = WorkbookFactory.create(is);
		excelSheetReader = new ExcelRepository("test.xls", workbook.getSheet("test"), entityMetaFactory,
				attrMetaFactory);
	}

	@AfterMethod
	public void afterMethod()
	{
		IOUtils.closeQuietly(is);
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = MolgenisDataException.class)
	public void ExcelRepository()
	{
		new ExcelRepository("test.xls", workbook.getSheet("test_mergedcells"), entityMetaFactory, attrMetaFactory);
	}

	@Test
	public void addCellProcessor_header()
	{
		CellProcessor processor = mock(CellProcessor.class);
		when(processor.processHeader()).thenReturn(true);
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		excelSheetReader.addCellProcessor(processor);
		for (@SuppressWarnings("unused") Entity entity : excelSheetReader)
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
		for (Entity entity : excelSheetReader)
			entity.get("col2");

		verify(processor).process("val2");
		verify(processor).process("val4");
		verify(processor).process("val6");
	}

	@Test
	public void getAttribute()
	{
		AttributeMetaData attr = excelSheetReader.getEntityMetaData().getAttribute("col1");
		assertNotNull(attr);
		assertEquals(attr.getDataType(), AttributeType.STRING);
		assertEquals(attr.getName(), "col1");
	}

	@Test
	public void getAttributes()
	{
		Iterator<AttributeMetaData> it = excelSheetReader.getEntityMetaData().getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
		assertFalse(it.hasNext());
	}

	@Test
	public void getDescription()
	{
		assertNull(excelSheetReader.getEntityMetaData().getDescription());
	}

	@Test
	public void getIdAttribute()
	{
		assertNull(excelSheetReader.getEntityMetaData().getIdAttribute());
	}

	@Test
	public void getLabel()
	{
		assertEquals(excelSheetReader.getEntityMetaData().getLabel(), "test");
	}

	@Test
	public void getLabelAttribute()
	{
		assertNull(excelSheetReader.getEntityMetaData().getLabelAttribute());
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
	public void iterator()
	{
		Iterator<Entity> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}

	@Test
	public void attributesAndIterator() throws IOException
	{
		Iterator<AttributeMetaData> headerIt = excelSheetReader.getEntityMetaData().getAttributes().iterator();
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col1");
		assertTrue(headerIt.hasNext());
		assertEquals(headerIt.next().getName(), "col2");

		Iterator<Entity> it = excelSheetReader.iterator();
		assertTrue(it.hasNext());

		Entity row1 = it.next();
		assertEquals(row1.get("col1"), "val1");
		assertEquals(row1.get("col2"), "val2");
		assertTrue(it.hasNext());

		Entity row2 = it.next();
		assertEquals(row2.get("col1"), "val3");
		assertEquals(row2.get("col2"), "val4");
		assertTrue(it.hasNext());

		Entity row3 = it.next();
		assertEquals(row3.get("col1"), "XXX");
		assertEquals(row3.get("col2"), "val6");
		assertTrue(it.hasNext());

		// test number cell (col1) and formula cell (col2)
		Entity row4 = it.next();
		assertEquals(row4.get("col1"), "1.2");
		assertEquals(row4.get("col2"), "2.4");
		assertFalse(it.hasNext());
	}
}
