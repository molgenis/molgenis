package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.file.processor.LowerCaseProcessor;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.*;

import static org.apache.poi.ss.usermodel.CellType.*;
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
		cellProcessors = new ArrayList<>();
		row = mock(Row.class);

		cell = mock(Cell.class);
		when(row.getCell(0)).thenReturn(cell);

		cellProcessors.add(new LowerCaseProcessor());
		colNamesMap = new LinkedHashMap<>();
		colNamesMap.put("attr1", 0);

		excelEntity = new ExcelEntity(row, colNamesMap, cellProcessors, mock(EntityType.class));
	}

	@Test
	public void getStringType()
	{
		when(cell.getCellTypeEnum()).thenReturn(STRING);
		when(cell.getStringCellValue()).thenReturn("XXX");

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "xxx");
	}

	@Test
	public void getBlankType()
	{
		when(cell.getCellTypeEnum()).thenReturn(BLANK);
		Object val = excelEntity.get("attr1");
		assertNull(val);
	}

	@Test
	public void getIntegerType()
	{
		when(cell.getCellTypeEnum()).thenReturn(NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1d);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "1");
	}

	@Test
	public void getDoubleType()
	{
		when(cell.getCellTypeEnum()).thenReturn(NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1.8d);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "1.8");
	}

	@Test
	public void getNumericDateType()
	{
		double dateDouble = 35917.0;
		TimeZone utcTimeZone = TimeZone.getTimeZone(ZoneId.of("UTC"));
		Date javaDate = DateUtil.getJavaDate(dateDouble, utcTimeZone);

		when(cell.getCellTypeEnum()).thenReturn(NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(dateDouble);
		when(cell.getDateCellValue()).thenReturn(javaDate);
		CellStyle cellStyle = mock(CellStyle.class);
		when(cell.getCellStyle()).thenReturn(cellStyle);
		short dataFormat = 0x0e;
		when(cellStyle.getDataFormat()).thenReturn(dataFormat);

		Object val = excelEntity.get("attr1");

		assertNotNull(val);
		assertEquals(val, "1998-05-02t00:00");
	}

	@Test
	public void getBooleanType()
	{
		when(cell.getCellTypeEnum()).thenReturn(BOOLEAN);
		when(cell.getBooleanCellValue()).thenReturn(true);

		Object val = excelEntity.get("attr1");
		assertNotNull(val);
		assertEquals(val, "true");
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void getErrorType()
	{
		when(cell.getCellTypeEnum()).thenReturn(ERROR);
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
