package org.molgenis.data.excel.simple;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.excel.simple.exception.MaximumSheetNameLengthExceededException;

public class ExcelWriter implements AutoCloseable {

  public static final int MAXIMUM_SHEET_LENGTH = 31;
  private final Path target;
  private final Workbook workbook;

  ExcelWriter(Path target, Workbook workbook) {
    this.target = requireNonNull(target);
    this.workbook = requireNonNull(workbook);
  }

  public boolean hasSheet(String name) {
    return workbook.getSheet(name) != null;
  }

  public void createSheet(String name, List<Object> headers) {
    if (name.length() <= MAXIMUM_SHEET_LENGTH) {
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
              int index = counter.getAndIncrement();
              if (record != null) {
                String stringValue = record.toString();
                if (!stringValue.isEmpty()) {
                  final Cell cell = row.createCell(index);
                  cell.setCellValue(stringValue);
                }
              }
            });
  }

  public void writeRow(List<Object> row, String sheetName) {
    this.writeRows(Stream.of(row), sheetName);
  }

  public void writeRows(List<List<Object>> rows, String sheetName) {
    this.writeRows(rows.stream(), sheetName);
  }

  public void writeRows(Stream<List<Object>> rows, String sheetName) {
    Sheet sheet = workbook.getSheet(sheetName);
    rows.forEach(
        row -> {
          internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
        });
  }

  public void close() throws IOException {
    try {
      workbook.write(Files.newOutputStream(target));
    } catch (IOException e) {
      throw e;
    } finally {
      workbook.close();
    }
  }
}
