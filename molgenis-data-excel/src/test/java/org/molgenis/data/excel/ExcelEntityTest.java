package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.processor.LowerCaseProcessor;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

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

		excelEntity = new ExcelEntity(row, colNamesMap, cellProcessors, mock(EntityType.class));
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

	@Test
	public void set()
	{
		excelEntity.set("attr1", "test");
		assertEquals(excelEntity.get("attr1"), "test");
	}

	@Test
	public void setEntity()
	{
		Entity entity = new DynamicEntity(mock(EntityType.class))
		{
			@Override
			protected void validateValueType(String attrName, Object value)
			{
				// noop
			}
		};
		entity.set("attr1", "test1");
		entity.set("attr2", "test2");

		excelEntity.set(entity);
		assertEquals(excelEntity.get("attr1"), "test1");
		assertNull(excelEntity.get("attr2"));
	}
}
