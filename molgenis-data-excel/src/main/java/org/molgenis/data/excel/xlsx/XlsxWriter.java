package org.molgenis.data.excel.xlsx;

import static java.util.Objects.requireNonNull;
import static jdk.nashorn.internal.runtime.JSType.toLong;
import static org.molgenis.data.DataConverter.toBoolean;
import static org.molgenis.data.DataConverter.toDouble;
import static org.molgenis.data.DataConverter.toInt;
import static org.molgenis.data.DataConverter.toLocalDate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
  private final TimeZone timeZone;

  private SimpleDateFormat simpleDateFormat;
  private SimpleDateFormat simpleDateTimeFormat;

  /** WARNING: This Class is not threadsafe because of the data formats!{@link SimpleDateFormat} */
  XlsxWriter(Path target, Workbook workbook, TimeZone timeZone) {
    this.target = requireNonNull(target);
    this.workbook = requireNonNull(workbook);
    this.timeZone = requireNonNull(timeZone);

    this.simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
    simpleDateFormat.setTimeZone(timeZone);
    this.simpleDateTimeFormat = new SimpleDateFormat("YYYY-MM-dd'T'hh:mm:ssZ");
    simpleDateTimeFormat.setTimeZone(timeZone);
  }

  public boolean hasSheet(String name) {
    try {
      return workbook.getSheet(name) != null;
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
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
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
    }
  }

  /**
   * @param row List of Objects, allowed Object classes: Boolean, LocalDate, Instant, Double,
   *     Integer, Long, String
   * @param sheetName
   */
  public void writeRow(List<Object> row, String sheetName) {
    try {
      this.writeRows(Stream.of(row), sheetName);
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
    }
  }

  /**
   * @param rows List of Lists of Objects, allowed Object classes: Boolean, LocalDate, Instant,
   *     Double, Integer, Long, String
   * @param sheetName
   */
  public void writeRows(List<List<Object>> rows, String sheetName) {
    try {
      this.writeRows(rows.stream(), sheetName);
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
    }
  }

  /**
   * @param rows Stream of Lists of Objects, allowed Object classes: Boolean, LocalDate, Instant,
   *     Double, Integer, Long, String
   * @param sheetName
   */
  public void writeRows(Stream<List<Object>> rows, String sheetName) {
    try {
      Sheet sheet = workbook.getSheet(sheetName);
      rows.forEach(
          row -> {
            internalWriteRow(row, sheet, sheet.getLastRowNum() + 1);
          });
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
    }
  }

  public void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
    this.simpleDateFormat = simpleDateFormat;
  }

  public void setSimpleDateTimeFormat(SimpleDateFormat simpleDateTimeFormat) {
    this.simpleDateTimeFormat = simpleDateTimeFormat;
  }

  @Override
  public void close() throws IOException {
    try {
      workbook.write(Files.newOutputStream(target));
    } catch (RuntimeException e) {
      throw new XlsxWriterException(e);
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
      Instant instant = toLocalDate(value).atStartOfDay(timeZone.toZoneId()).toInstant();
      cell.setCellValue(simpleDateFormat.format(Date.from(instant)));
    } else if (value instanceof Instant) {
      cell.setCellValue(simpleDateTimeFormat.format(Date.from((Instant) value)));
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
