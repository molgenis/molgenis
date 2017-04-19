package org.molgenis.data.excel;

import org.apache.poi.ss.usermodel.*;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ExcelUtilsTest
{
	// regression test: https://github.com/molgenis/molgenis/issues/6048
	@Test
	public void testToValueNumericLong() throws Exception
	{
		Cell cell = mock(Cell.class);
		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
		assertEquals(ExcelUtils.toValue(cell), "12342151234");
	}

	@Test
	public void testToValueFormulaNumericLong() throws Exception
	{
		CellValue cellValue = new CellValue(1.2342151234E10);

		Cell cell = mock(Cell.class);

		FormulaEvaluator formulaEvaluator = mock(FormulaEvaluator.class);
		when(formulaEvaluator.evaluate(cell)).thenReturn(cellValue);

		CreationHelper creationHelper = mock(CreationHelper.class);
		when(creationHelper.createFormulaEvaluator()).thenReturn(formulaEvaluator);

		Workbook workbook = mock(Workbook.class);
		when(workbook.getCreationHelper()).thenReturn(creationHelper);

		Sheet sheet = mock(Sheet.class);
		when(sheet.getWorkbook()).thenReturn(workbook);

		when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_FORMULA);
		when(cell.getSheet()).thenReturn(sheet);
		when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
		assertEquals(ExcelUtils.toValue(cell), "12342151234");
	}
}