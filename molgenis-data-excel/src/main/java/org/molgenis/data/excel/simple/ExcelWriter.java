package org.molgenis.data.excel.simple;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

  public boolean hasSheet(String name) {
    return workbook.getSheet(name) != null;
  }

  public void createSheet(String name, List<Object> headers) throws IOException {
    if (name.length() <= 31) {
      Sheet sheet = workbook.getSheet(name);
      if (sheet == null) {
        sheet = workbook.createSheet(name);
        internalWriteRow(headers, sheet, 0);
      }
    } else {
      throw new MaximumSheetNameLengthExceededException(name);
    }
  }

  private void internalWriteRow(List<Object> values, Sheet sheet, int rowNr) {
    final org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNr);
    AtomicInteger counter = new AtomicInteger(0);
    values
        .stream()
        .forEach(
            record -> {
              String stringValue = record.toString().trim();
              int index = counter.getAndIncrement();
              if (record != null && !stringValue.isEmpty()) {
                final Cell cell = row.createCell(index);
                cell.setCellValue(stringValue);
              }
            });
  }

  public void writeRow(List<Object> row, String sheetName) throws IOException {
    this.writeRows(Stream.of(row), sheetName);
  }

  public void writeRows(List<List<Object>> rows, String sheetName) throws IOException {
    this.writeRows(rows.stream(), sheetName);
  }

  public void writeRows(Stream<List<Object>> rows, String sheetName) throws IOException {
    Sheet sheet = workbook.getSheet(sheetName);
    rows.forEach(
        row -> {
          internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
        });
  }

  public void close() throws IOException {
    workbook.write(new FileOutputStream(target));
    workbook.close();
  }
}
