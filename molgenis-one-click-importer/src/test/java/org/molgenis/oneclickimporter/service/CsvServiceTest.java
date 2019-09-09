package org.molgenis.oneclickimporter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.oneclickimporter.service.utils.OneClickImporterTestUtils.loadFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.oneclickimporter.service.impl.CsvServiceImpl;

class CsvServiceTest {
  private CsvService csvService = new CsvServiceImpl();

  @Test
  void buildLinesFromFileTest()
      throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException {
    List<String[]> actual =
        csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/simple-valid.csv"));
    List<String[]> expected = new ArrayList<>();
    expected.add(new String[] {"name", "superpower"});
    expected.add(new String[] {"Mark", "arrow functions"});
    expected.add(new String[] {"Connor", "Oldschool syntax"});
    expected.add(new String[] {"Fleur", "Lambda Magician"});
    expected.add(new String[] {"Dennis", "Root access"});

    assertEquals(actual.size(), expected.size());
    for (int i = 0; i < actual.size(); ++i) {
      assertArrayEquals(actual.get(i), expected.get(i));
    }
  }

  @Test
  void buildLinesWithEmptyFile()
      throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException {
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> csvService.buildLinesFromFile(loadFile(CsvServiceTest.class, "/empty-file.csv")));
    assertThat(exception.getMessage()).containsPattern("CSV-file: \\[empty-file.csv\\] is empty");
  }

  @Test
  void buildLinesWithHeaderOnly()
      throws InvalidFormatException, IOException, URISyntaxException, MolgenisDataException {
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () ->
                csvService.buildLinesFromFile(
                    loadFile(CsvServiceTest.class, "/header-without-data.csv")));
    assertThat(exception.getMessage())
        .containsPattern(
            "Header was found, but no data is present in file \\[header-without-data.csv\\]");
  }
}
