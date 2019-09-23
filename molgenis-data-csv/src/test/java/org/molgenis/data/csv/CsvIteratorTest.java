package org.molgenis.data.csv;

import static com.google.common.collect.Iterators.size;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

class CsvIteratorTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private EntityType entityType;

  @BeforeEach
  void setUpBeforeMethod() {
    entityType = entityTypeFactory.create();
    entityType.addAttribute(attrMetaFactory.create().setName("col1"));
    entityType.addAttribute(attrMetaFactory.create().setName("col2"));
  }

  @Test
  void testIteratorFromCsvFile() throws IOException {
    File csvFile = new ClassPathResource("testdata.csv").getFile();
    CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));

    it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    Entity entity = it.next();
    assertEquals("val1", entity.get("col1"));
    assertEquals("val2", entity.get("col2"));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testIteratorValueHeaderMismatchOneNonEmptyValue() throws IOException {
    File csvFile = new ClassPathResource("testdatamissingvalue.csv").getFile();
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> new CsvIterator(csvFile, "testdatamissingvalue", null, ',', entityType).next());
    assertThat(exception.getMessage())
        .containsPattern(
            "Number of values \\(1\\) doesn't match the number of headers \\(2\\): \\[val1\\]");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testIteratorValueHeaderMismatchTooLittle() throws IOException {
    File csvFile = new ClassPathResource("testdatamissingvalue2.csv").getFile();
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> new CsvIterator(csvFile, "testdatamissingvalue", null, ',', entityType).next());
    assertThat(exception.getMessage())
        .containsPattern(
            "Number of values \\(2\\) doesn't match the number of headers \\(3\\): \\[val1,val2\\]");
  }

  @SuppressWarnings("deprecation")
  @Test
  void testIteratorValueHeaderMismatchTooMuch() throws IOException {
    File csvFile = new ClassPathResource("testdatamissingvalue3.csv").getFile();
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> new CsvIterator(csvFile, "testdatamissingvalue", null, ',', entityType).next());
    assertThat(exception.getMessage())
        .containsPattern(
            "Number of values \\(4\\) doesn't match the number of headers \\(3\\): \\[val1,val2,val3,val4\\]");
  }

  @Test
  void testIteratorFromZipFile() throws IOException {
    File zipFile = new ClassPathResource("zipFile.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));
  }

  @Test
  void testIteratorFromZipFileWithFolder() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithFolder.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));
  }

  @Test
  void testIteratorFromCsvFileWithBom() throws IOException {
    File csvFile = new ClassPathResource("testDataWithBom.csv").getFile();
    CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));

    it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    Entity entity = it.next();
    assertEquals("val1", entity.get("col1"));
    assertEquals("val2", entity.get("col2"));
  }

  @Test
  void testIteratorFromZipFileWithBom() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithBom.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));
  }

  @Test
  void testIteratorFromZipFileWithFolderWithBom() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithFolderWithBom.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
    assertEquals(newLinkedHashSet(asList("col1", "col2")), it.getColNamesMap().keySet());
    assertEquals(5, size(it));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testCsvIteratorDuplicateColumnHeader() throws IOException {
    File tmpFile = new ClassPathResource("duplicate-sheet-header.csv").getFile();
    Exception exception =
        assertThrows(
            MolgenisDataException.class,
            () -> new CsvIterator(tmpFile, "duplicate-sheet-header", null, ',', entityType));
    assertThat(exception.getMessage())
        .containsPattern("Duplicate column header 'col1' not allowed");
  }
}
