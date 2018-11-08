package org.molgenis.data.excel.simple;

import java.io.File;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriterFactory {

  private ExcelWriterFactory() {}

  public static ExcelWriter create(File target) {
    return new ExcelWriter(target, new XSSFWorkbook());
  }
}
