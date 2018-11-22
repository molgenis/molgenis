package org.molgenis.data.excel.xlsx;

import static java.util.Objects.requireNonNull;
import static jdk.nashorn.internal.runtime.JSType.toLong;
import static org.molgenis.data.DataConverter.toBoolean;
import static org.molgenis.data.DataConverter.toDouble;
import static org.molgenis.data.DataConverter.toInstant;
import static org.molgenis.data.DataConverter.toInt;
import static org.molgenis.data.DataConverter.toLocalDate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.molgenis.data.excel.xlsx.exception.MaximumSheetNameLengthExceededException;
import org.molgenis.data.excel.xlsx.exception.UnsupportedValueException;
import org.molgenis.data.excel.xlsx.exception.XlsxWriterException;

public class XlsxWriter implements AutoCloseable {

  // Apache poi library cuts of sheet names at 31 characters
  public static final int MAXIMUM_SHEET_LENGTH = 31;
  private final Path target;
  private final Workbook workbook;

  XlsxWriter(Path target, Workbook workbook) {
    this.target = requireNonNull(target);
    this.workbook = requireNonNull(workbook);
  }

  public boolean hasSheet(String name) {
    try {
      return workbook.getSheet(name) != null;
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    }
  }

  public void createSheet(String name, List<Object> headers) {
    try {
      if (name.length() <= MAXIMUM_SHEET_LENGTH) {
        Sheet sheet = workbook.getSheet(name);
        if (sheet == null) {
          sheet = workbook.createSheet(name);
          internalWriteRow(headers, sheet, 0);
        }
      } else {
        throw new MaximumSheetNameLengthExceededException(name);
      }
    } catch (MaximumSheetNameLengthExceededException e) {
      throw e;
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    }
  }

  public void writeRow(List<Object> row, String sheetName) {
    try {
      this.writeRows(Stream.of(row), sheetName);
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    }
  }

  public void writeRows(List<List<Object>> rows, String sheetName) {
    try {
      this.writeRows(rows.stream(), sheetName);
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    }
  }

  public void writeRows(Stream<List<Object>> rows, String sheetName) {
    try {
      Sheet sheet = workbook.getSheet(sheetName);
      rows.forEach(
          row -> {
            internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
          });
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    }
  }

  public void close() throws IOException {
    try {
      workbook.write(Files.newOutputStream(target));
    } catch (Throwable throwable) {
      throw new XlsxWriterException(throwable);
    } finally {
      workbook.close();
    }
  }

  private void internalWriteRow(List<Object> values, Sheet sheet, int rowNr) {
    final Row row = sheet.createRow(rowNr);
    AtomicInteger counter = new AtomicInteger(0);
    values
        .stream()
        .forEach(
            record -> {
              int index = counter.getAndIncrement();
              if (record != null) {
                Cell cell = row.createCell(index);
                setCellValue(cell, record);
              }
            });
  }

  protected void setCellValue(Cell cell, Object value) {
    if (value instanceof Boolean) {
      cell.setCellValue(toBoolean(value));
    } else if (value instanceof LocalDate) {
      Instant instant = toLocalDate(value).atStartOfDay(ZoneId.systemDefault()).toInstant();
      Date date = Date.from(instant);
      cell.setCellValue(date);
    } else if (value instanceof Instant) {
      cell.setCellValue(Date.from(toInstant(value)));
    } else if (value instanceof Double) {
      cell.setCellValue(toDouble(value));
    } else if (value instanceof Integer) {
      cell.setCellValue(toInt(value));
    } else if (value instanceof Long) {
      cell.setCellValue(toLong(value));
    } else if (value instanceof String) {
      cell.setCellValue(value.toString());
    } else {
      throw new UnsupportedValueException(value);
    }
  }
}
