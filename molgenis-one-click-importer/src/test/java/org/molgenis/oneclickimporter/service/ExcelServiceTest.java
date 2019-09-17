package org.molgenis.oneclickimporter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.oneclickimporter.exceptions.EmptySheetException;
import org.molgenis.oneclickimporter.service.impl.ExcelServiceImpl;

class ExcelServiceTest {
  private ExcelService excelService = new ExcelServiceImpl();

  @Test
  void buildExcelSheetsWithEmptyFile()
      throws InvalidFormatException, IOException, URISyntaxException, EmptySheetException {
    Exception exception =
        assertThrows(
            EmptySheetException.class,
            () ->
                excelService.buildExcelSheetsFromFile(
                    loadFile(ExcelServiceTest.class, "/empty-sheet.xlsx")));
    assertThat(exception.getMessage()).containsPattern("Sheet \\[empty_sheet\\] is empty");
  }

  @Test
  void buildExcelSheetsWithHeaderOnly()
      throws InvalidFormatException, IOException, URISyntaxException, EmptySheetException {
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () ->
                excelService.buildExcelSheetsFromFile(
                    loadFile(ExcelServiceTest.class, "/header-without-data.xlsx")));
    assertThat(exception.getMessage())
        .containsPattern("Header was found, but no data is present in sheet \\[Sheet1\\]");
  }
}
