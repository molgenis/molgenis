package org.molgenis.data.excel.xlsx;

import java.nio.file.Path;
import java.util.TimeZone;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class XlsxWriterFactory {

  public static final int ROWS_IN_MEMORY = 1000;

  private XlsxWriterFactory() {}

  public static XlsxWriter create(Path target, TimeZone timeZone) {
    return new XlsxWriter(target, new SXSSFWorkbook(ROWS_IN_MEMORY), timeZone);
  }
}
