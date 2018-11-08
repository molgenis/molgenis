package org.molgenis.data.excel.simple;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.excel.simple.exception.MaximumSheetNameLengthExceededException;

public class ExcelWriter implements AutoCloseable {

  private final File target;
  private Workbook workbook;

  ExcelWriter(File target, Workbook workbook) {
    this.target = requireNonNull(target);
    this.workbook = requireNonNull(workbook);
  }

  private void write() throws IOException {
    workbook.write(new FileOutputStream(target));
  }

  public boolean hasSheet(String name) {
    return workbook.getSheet(name) != null;
  }

  public void createSheet(String name, List<Object> headers) throws IOException {
    if (name.length() <= 31) {
      Sheet sheet = workbook.getSheet(name);
      if (sheet == null) {
        sheet = workbook.createSheet(name);
        internalWriteRow(headers, sheet, 0);
        write();
      }
    } else {
      throw new MaximumSheetNameLengthExceededException(name);
    }
  }

  private void internalWriteRow(List<Object> values, Sheet sheet, int rowNr) {
    final org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNr);
    for (int index = 0; index < values.size(); index++) {
      final Object record = values.get(index);
      if (record != null && !record.toString().trim().isEmpty()) {
        final Cell cell = row.createCell(index);
        cell.setCellValue(record.toString().trim());
      }
    }
  }

  public void writeRow(List<Object> row, String sheetName) throws IOException {
    this.writeRows(Stream.of(row), sheetName, 1);
  }

  public void writeRows(List<List<Object>> rows, String sheetName, int batchSize)
      throws IOException {
    this.writeRows(rows.stream(), sheetName, batchSize);
  }

  public void writeRows(Stream<List<Object>> rows, String sheetName, int batchSize)
      throws IOException {
    int i = 0;
    Iterator<List<Object>> iter = rows.iterator();
    while (iter.hasNext()) {
      List<Object> row = iter.next();
      Sheet sheet = workbook.getSheet(sheetName);
      internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
      i++;
      if ((i % batchSize) == 0) {
        write();
      }
    }
    write(); // write remaining rows to file
  }

  public void close() throws IOException {
    workbook.close();
  }
}
