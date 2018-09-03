package org.molgenis.data.csv;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CsvIteratorTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private EntityType entityType;

  @BeforeMethod
  public void setUpBeforeMethod() {
    entityType = entityTypeFactory.create();
    entityType.addAttribute(attrMetaFactory.create().setName("col1"));
    entityType.addAttribute(attrMetaFactory.create().setName("col2"));
  }

  @Test
  public void testIteratorFromCsvFile() throws IOException {
    File csvFile = new ClassPathResource("testdata.csv").getFile();
    CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);

    it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    Entity entity = it.next();
    assertEquals(entity.get("col1"), "val1");
    assertEquals(entity.get("col2"), "val2");
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisDataException.class,
      expectedExceptionsMessageRegExp =
          "Number of values \\(1\\) doesn't match the number of headers \\(2\\): \\[val1\\]")
  public void testIteratorValueHeaderMismatchOneNonEmptyValue() throws IOException {
    File csvFile = new ClassPathResource("testdatamissingvalue.csv").getFile();
    new CsvIterator(csvFile, "testdatamissingvalue", null, ',', entityType).next();
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisDataException.class,
      expectedExceptionsMessageRegExp =
          "Number of values \\(2\\) doesn't match the number of headers \\(3\\): \\[val1,val2\\]")
  public void testIteratorValueHeaderMismatch() throws IOException {
    File csvFile = new ClassPathResource("testdatamissingvalue2.csv").getFile();
    new CsvIterator(csvFile, "testdatamissingvalue", null, ',', entityType).next();
  }

  @Test
  public void testIteratorFromZipFile() throws IOException {
    File zipFile = new ClassPathResource("zipFile.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);
  }

  @Test
  public void testIteratorFromZipFileWithFolder() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithFolder.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);
  }

  @Test
  public void testIteratorFromCsvFileWithBom() throws IOException {
    File csvFile = new ClassPathResource("testDataWithBom.csv").getFile();
    CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);

    it = new CsvIterator(csvFile, "testdata", null, null, entityType);
    Entity entity = it.next();
    assertEquals(entity.get("col1"), "val1");
    assertEquals(entity.get("col2"), "val2");
  }

  @Test
  public void testIteratorFromZipFileWithBom() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithBom.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);
  }

  @Test
  public void testIteratorFromZipFileWithFolderWithBom() throws IOException {
    File zipFile = new ClassPathResource("zipFileWithFolderWithBom.zip").getFile();
    CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
    assertEquals(
        it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
    assertEquals(Iterators.size(it), 5);
  }

  @SuppressWarnings("deprecation")
  @Test(
      expectedExceptions = MolgenisDataException.class,
      expectedExceptionsMessageRegExp = "Duplicate column header 'col1' not allowed")
  public void testCsvIteratorDuplicateColumnHeader() throws IOException {
    File tmpFile = new ClassPathResource("duplicate-sheet-header.csv").getFile();
    new CsvIterator(tmpFile, "duplicate-sheet-header", null, ',', entityType);
  }
}
