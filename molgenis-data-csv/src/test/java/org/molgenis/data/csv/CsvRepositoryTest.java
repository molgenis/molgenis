package org.molgenis.data.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

@MockitoSettings(strictness = Strictness.LENIENT)
class CsvRepositoryTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private static File test;
  private static File testdata;
  private static File novalues;
  private static File emptyvalues;
  private static File testtsv;
  private static File emptylines;
  private static File emptylinessinglecol;

  @BeforeAll
  static void beforeClass() throws IOException {
    test = new ClassPathResource("test.csv").getFile();
    testdata = new ClassPathResource("testdata.csv").getFile();
    novalues = new ClassPathResource("novalues.csv").getFile();
    emptyvalues = new ClassPathResource("emptyvalues.csv").getFile();
    testtsv = new ClassPathResource("test.tsv").getFile();
    emptylines = new ClassPathResource("emptylines.csv").getFile();
    emptylinessinglecol = new ClassPathResource("emptylinessinglecol.csv").getFile();
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  public void addCellProcessorHeader() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();
    when(processor.process("col1")).thenReturn("col1");
    when(processor.process("col2")).thenReturn("col2");

    try (CsvRepository csvRepository =
        new CsvRepository(test, entityTypeFactory, attrMetaFactory, null)) {
      csvRepository.addCellProcessor(processor);
      for (@SuppressWarnings("unused") Entity entity : csvRepository) {}
      verify(processor).process("col1");
      verify(processor).process("col2");
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  public void addCellProcessorData() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
    try (CsvRepository csvRepository =
        new CsvRepository(test, entityTypeFactory, attrMetaFactory, null)) {
      csvRepository.addCellProcessor(processor);
      for (@SuppressWarnings("unused") Entity entity : csvRepository) {}
      verify(processor).process("val1");
      verify(processor).process("val2");
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  public void addCellProcessorConstructor() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
    try (CsvRepository csvRepository =
        new CsvRepository(test, entityTypeFactory, attrMetaFactory, List.of(processor))) {
      for (@SuppressWarnings("unused") Entity entity : csvRepository) {}
      verify(processor).process("val1");
      verify(processor).process("val2");
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Test
  public void addCellProcessorConstructor2() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
    try (CsvRepository csvRepository =
        new CsvRepository(test, entityTypeFactory, attrMetaFactory, List.of(processor), ',')) {
      for (@SuppressWarnings("unused") Entity entity : csvRepository) {}
      verify(processor).process("val1");
      verify(processor).process("val2");
    }
  }

  @Test
  void metaData() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null)) {
      assertEquals("testdata", csvRepository.getName());
      Iterator<Attribute> it = csvRepository.getEntityType().getAttributes().iterator();
      assertTrue(it.hasNext());
      assertEquals("col1", it.next().getName());
      assertTrue(it.hasNext());
      assertEquals("col2", it.next().getName());
      assertFalse(it.hasNext());
    }
  }

  /** Test based on au.com.bytecode.opencsv.CSVReaderTest */
  @Test
  void iterator() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();

      assertTrue(it.hasNext());
      Entity entity = it.next();
      assertEquals("val1", entity.get("col1"));
      assertEquals("val2", entity.get("col2"));

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals("a,a", entity.get("col1"));
      assertEquals("b", entity.get("col2"));
      assertTrue(it.hasNext());

      assertTrue(it.hasNext());
      entity = it.next();
      assertNull(entity.get("col1"));
      assertEquals("a", entity.get("col2"));

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals("\"", entity.get("col1"));
      assertEquals("\"\"", entity.get("col2"));

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals(",", entity.get("col1"));
      assertEquals(",,", entity.get("col2"));

      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorNoValues() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(novalues, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorEmptyValues() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptyvalues, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      assertTrue(it.hasNext());
      assertNull(it.next().get("col1"));
    }
  }

  @Test
  public void iteratorTsv() throws IOException {
    try (CsvRepository tsvRepository =
        new CsvRepository(testtsv, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = tsvRepository.iterator();
      Entity entity = it.next();
      assertEquals("val1", entity.get("col1"));
      assertEquals("val2", entity.get("col2"));
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorEmptyLines() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptylines, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      Entity entity = it.next();
      assertEquals("val1", entity.get("col1"));
      assertEquals("val2", entity.get("col2"));
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorEmptyLinesSingleCol() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptylinessinglecol, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      Entity entity = it.next();
      assertEquals("val1", entity.get("col1"));

      assertTrue(it.hasNext());
      entity = it.next();
      assertNull(entity.get("col1"));

      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorCaseSensitivity() throws IOException {
    File csvFile = ResourceUtils.getFile("case-sensitivity.csv");
    try (CsvRepository csvRepository =
        new CsvRepository(csvFile, entityTypeFactory, attrMetaFactory, null)) {
      Entity entity = csvRepository.iterator().next();
      assertEquals("Value #0", entity.get("Header"));
      assertNull(entity.get("hEADER"));
    }
  }
}
