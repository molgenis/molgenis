package org.molgenis.data.excel.simple;

import java.io.File;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelWriterFactory {

  public static final int ROWS_IN_MEMORY = 10000;

  private ExcelWriterFactory() {}

  public static ExcelWriter create(File target) {
    return new ExcelWriter(target, new SXSSFWorkbook(ROWS_IN_MEMORY));
  }
}
