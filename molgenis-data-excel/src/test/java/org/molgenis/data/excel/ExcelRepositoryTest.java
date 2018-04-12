package org.molgenis.data.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ExcelRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private ExcelRepository excelSheetReader;

	private Workbook workbook;
	private InputStream is;

	public ExcelRepositoryTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod() throws InvalidFormatException, IOException
	{
		is = getClass().getResourceAsStream("/test.xls");
		workbook = WorkbookFactory.create(is);
		excelSheetReader = new ExcelRepository("test.xls", workbook.getSheet("test"), entityTypeFactory,
				attrMetaFactory);
	}

	@AfterMethod
	public void afterMethod() throws IOException
	{
		is.close();
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = MolgenisDataException.class)
	public void ExcelRepository()
	{
		new ExcelRepository("test.xls", workbook.getSheet("test_mergedcells"), entityTypeFactory, attrMetaFactory);
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
		Attribute attr = excelSheetReader.getEntityType().getAttribute("col1");
		assertNotNull(attr);
		assertEquals(attr.getDataType(), AttributeType.STRING);
		assertEquals(attr.getName(), "col1");
	}

	@Test
	public void getAttributes()
	{
		Iterator<Attribute> it = excelSheetReader.getEntityType().getAttributes().iterator();
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col1");
		assertTrue(it.hasNext());
		assertEquals(it.next().getName(), "col2");
		assertFalse(it.hasNext());
	}

	@Test
	public void getDescription()
	{
		assertNull(excelSheetReader.getEntityType().getDescription());
	}

	@Test
	public void getIdAttribute()
	{
		assertNull(excelSheetReader.getEntityType().getIdAttribute());
	}

	@Test
	public void getLabel()
	{
		assertEquals(excelSheetReader.getEntityType().getLabel(), "test");
	}

	@Test
	public void getLabelAttribute()
	{
		assertNull(excelSheetReader.getEntityType().getLabelAttribute());
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

	@Test(expectedExceptions = NoSuchElementException.class)
	public void iteratorNextWhenNoNext()
	{
		Iterator<Entity> it = excelSheetReader.iterator();
		it.next(); // 1
		it.next(); // 2
		it.next(); // 3
		it.next(); // 4
		it.next(); // does not exist
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Duplicate column header 'entity' in sheet 'attributes' not allowed")
	public void iteratorDuplicateSheetHeader() throws IOException, InvalidFormatException
	{
		String fileName = "/duplicate-sheet-header.xlsx";
		try (InputStream inputStream = getClass().getResourceAsStream(fileName))
		{
			Workbook workbook = WorkbookFactory.create(inputStream);
			ExcelRepository excelRepository = new ExcelRepository(fileName, workbook.getSheet("attributes"),
					entityTypeFactory, attrMetaFactory);
			excelRepository.iterator();
		}
	}

	@Test
	public void attributesAndIterator() throws IOException
	{
		Iterator<Attribute> headerIt = excelSheetReader.getEntityType().getAttributes().iterator();
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
