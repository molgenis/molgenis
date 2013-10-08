package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.io.processor.CellProcessor;
import org.molgenis.io.processor.LowerCaseProcessor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelEntityTest
{
	private ExcelEntity excelEntity;
	private List<CellProcessor> cellProcessors;
	private Row row;
	private Cell cell;
	private Map<String, Integer> colNamesMap;

	@BeforeMethod
	public void beforeMethod()
	{
		cellProcessors = new ArrayList<CellProcessor>();
		row = mock(Row.class);

		cell = mock(Cell.class);
		when(row.getCell(0)).thenReturn(cell);

		cellProcessors.add(new LowerCaseProcessor());
		colNamesMap = new LinkedHashMap<String, Integer>();
		colNamesMap.put("attr1", 0);

		excelEntity = new ExcelEntity(row, colNamesMap, cellProcessors, new DefaultEntityMetaData("Entity1"));
	}

	@Test
	public void getStringType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		when(cell.getStringCellValue()).thenReturn("XXX");

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "xxx");
	}

	@Test
	public void getBlankType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_BLANK);
		Object val = excelEntity.get("attr1");
		assertNull(val);
	}

	@Test
	public void getIntegerType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1d);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "1");
	}

	@Test
	public void getDoubleType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1.8d);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "1.8");
	}

	@Test
	public void getBooleanType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_BOOLEAN);
		when(cell.getBooleanCellValue()).thenReturn(true);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "true");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getErrorType()
	{
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_ERROR);
		excelEntity.get("attr1");
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void set()
	{
		excelEntity.set("attr1", "test");
	}
}
