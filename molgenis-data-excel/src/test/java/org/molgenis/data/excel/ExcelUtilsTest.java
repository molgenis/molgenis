package org.molgenis.data.excel;

import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.excel.ExcelUtils.getNumberOfSheets;
import static org.molgenis.data.excel.ExcelUtils.isExcelFile;
import static org.molgenis.data.excel.ExcelUtils.toValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.molgenis.util.ResourceUtils;

class ExcelUtilsTest {
  // regression test: https://github.com/molgenis/molgenis/issues/6048
  @Test
  void testToValueNumericLong() throws Exception {
    Cell cell = mock(Cell.class);
    when(cell.getCellTypeEnum()).thenReturn(NUMERIC);
    when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
    assertEquals("12342151234", toValue(cell));
  }

  @Test
  void testToValueFormulaNumericLong() throws Exception {
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

    when(cell.getCellTypeEnum()).thenReturn(FORMULA);
    when(cell.getSheet()).thenReturn(sheet);
    when(cell.getNumericCellValue()).thenReturn(1.2342151234E10);
    assertEquals("12342151234", toValue(cell));
  }

  @Test
  void renameSheetTest() throws IOException, InvalidFormatException {
    File file = ResourceUtils.getFile(getClass(), "/test.xls");
    File temp = File.createTempFile("unittest_", ".xls");
    FileUtils.copyFile(file, temp);
    ExcelUtils.renameSheet("unittest", temp, 0);
    Workbook workbook = WorkbookFactory.create(new FileInputStream(temp));
    assertEquals("unittest", workbook.getSheetAt(0).getSheetName());
  }

  @Test
  void getNumberOfSheetsTest() {
    File file = ResourceUtils.getFile(getClass(), "/test.xls");
    assertEquals(3, getNumberOfSheets(file));
  }

  @Test
  void getNumberOfSheetsTestCSV() {
    File file = ResourceUtils.getFile(getClass(), "/test.csv");
    assertEquals(-1, getNumberOfSheets(file));
  }

  @Test
  void testIsExcelFileTrueXLSX() {
    assertEquals(true, isExcelFile("test.xlsx"));
  }

  @Test
  void testIsExcelFileTrueXLS() {
    assertEquals(true, isExcelFile("test.xls"));
  }

  @Test
  void testIsExcelFileFalse() {
    assertEquals(false, isExcelFile("test.csv"));
  }
}
