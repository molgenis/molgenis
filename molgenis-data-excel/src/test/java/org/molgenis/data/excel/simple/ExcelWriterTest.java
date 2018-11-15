package org.molgenis.data.excel.simple;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelWriterTest extends AbstractMockitoTest {

  @Mock Workbook workbook;

  @Mock Sheet sheet;

  File file;

  ExcelWriter excelWriter;

  @BeforeMethod
  public void setUp() {
    file = new File("path");
    excelWriter = new ExcelWriter(file.toPath(), workbook);
  }

  @Test
  public void testHasSheetTrue() {
    Sheet sheet = mock(Sheet.class);
    when(workbook.getSheet("test")).thenReturn(sheet);
    assertTrue(excelWriter.hasSheet("test"));
  }

  @Test
  public void testHasSheetFalse() {
    when(workbook.getSheet("test")).thenReturn(null);
    assertFalse(excelWriter.hasSheet("test"));
  }

  @Test
  public void testCreateSheet() throws IOException {
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    Row row = mock(Row.class);
    doReturn(cell0).when(row).createCell(0);
    doReturn(cell1).when(row).createCell(1);
    when(sheet.createRow(0)).thenReturn(row);
    when(workbook.getSheet("test")).thenReturn(null);
    when(workbook.createSheet("test")).thenReturn(sheet);

    excelWriter.createSheet("test", newArrayList("head1", "head2"));
    verify(cell0).setCellValue("head1");
    verify(cell1).setCellValue("head2");
  }

  @Test
  public void testWriteRow() throws IOException {
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    Row row = mock(Row.class);
    doReturn(cell0).when(row).createCell(0);
    doReturn(cell1).when(row).createCell(1);
    when(sheet.createRow(1)).thenReturn(row);
    when(workbook.getSheet("test")).thenReturn(sheet);

    excelWriter.writeRow(newArrayList("value1", "value2"), "test");
    verify(cell0).setCellValue("value1");
    verify(cell1).setCellValue("value2");
  }

  @Test
  public void testWriteRowsStream() throws IOException {
    int[] number = {-1};
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    Cell cell2 = mock(Cell.class);
    Cell cell3 = mock(Cell.class);

    Row row1 = mock(Row.class);
    Row row2 = mock(Row.class);

    doReturn(cell0).when(row1).createCell(0);
    doReturn(cell1).when(row1).createCell(1);
    doReturn(cell2).when(row2).createCell(0);
    doReturn(cell3).when(row2).createCell(1);

    doReturn(row1).when(sheet).createRow(1);
    doReturn(row2).when(sheet).createRow(2);

    Mockito.doAnswer(
            new Answer<Integer>() {
              @Override
              public Integer answer(InvocationOnMock invocation) throws Throwable {
                number[0]++;
                return number[0];
              }
            })
        .when(sheet)
        .getLastRowNum();

    when(workbook.getSheet("test")).thenReturn(sheet);

    excelWriter.writeRows(
        Stream.of(newArrayList("value1", "value2"), newArrayList("value3", "value4")), "test");

    verify(cell0).setCellValue("value1");
    verify(cell1).setCellValue("value2");
    verify(cell2).setCellValue("value3");
    verify(cell3).setCellValue("value4");
  }

  @Test
  public void testWriteRowsList() throws IOException {
    int[] number = {-1};
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    Cell cell2 = mock(Cell.class);
    Cell cell3 = mock(Cell.class);

    Row row1 = mock(Row.class);
    Row row2 = mock(Row.class);

    doReturn(cell0).when(row1).createCell(0);
    doReturn(cell1).when(row1).createCell(1);
    doReturn(cell2).when(row2).createCell(0);
    doReturn(cell3).when(row2).createCell(1);

    doReturn(row1).when(sheet).createRow(1);
    doReturn(row2).when(sheet).createRow(2);

    Mockito.doAnswer(
            new Answer<Integer>() {
              @Override
              public Integer answer(InvocationOnMock invocation) throws Throwable {
                number[0]++;
                return number[0];
              }
            })
        .when(sheet)
        .getLastRowNum();

    when(workbook.getSheet("test")).thenReturn(sheet);

    excelWriter.writeRows(
        newArrayList(newArrayList("value1", "value2"), newArrayList("value3", "value4")), "test");

    verify(cell0).setCellValue("value1");
    verify(cell1).setCellValue("value2");
    verify(cell2).setCellValue("value3");
    verify(cell3).setCellValue("value4");
  }

  @Test
  public void testClose() throws IOException {
    excelWriter.close();
    verify(workbook).close();
  }
}
