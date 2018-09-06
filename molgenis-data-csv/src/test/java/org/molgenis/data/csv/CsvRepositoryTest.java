package org.molgenis.data.csv;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CsvRepositoryTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private static File test;
  private static File testdata;
  private static File novalues;
  private static File emptyvalues;
  private static File testtsv;
  private static File emptylines;
  private static File emptylinessinglecol;

  public CsvRepositoryTest() {
    super(Strictness.WARN);
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
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
  public void addCellProcessor_header() throws IOException {
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
  public void addCellProcessor_data() throws IOException {
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

  @Test
  public void metaData() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null)) {
      assertEquals(csvRepository.getName(), "testdata");
      Iterator<Attribute> it = csvRepository.getEntityType().getAttributes().iterator();
      assertTrue(it.hasNext());
      assertEquals(it.next().getName(), "col1");
      assertTrue(it.hasNext());
      assertEquals(it.next().getName(), "col2");
      assertFalse(it.hasNext());
    }
  }

  /** Test based on au.com.bytecode.opencsv.CSVReaderTest */
  @Test
  public void iterator() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();

      assertTrue(it.hasNext());
      Entity entity = it.next();
      assertEquals(entity.get("col1"), "val1");
      assertEquals(entity.get("col2"), "val2");

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals(entity.get("col1"), "a,a");
      assertEquals(entity.get("col2"), "b");
      assertTrue(it.hasNext());

      assertTrue(it.hasNext());
      entity = it.next();
      assertNull(entity.get("col1"));
      assertEquals(entity.get("col2"), "a");

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals(entity.get("col1"), "\"");
      assertEquals(entity.get("col2"), "\"\"");

      assertTrue(it.hasNext());
      entity = it.next();
      assertEquals(entity.get("col1"), ",");
      assertEquals(entity.get("col2"), ",,");

      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iterator_noValues() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(novalues, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iterator_emptyValues() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptyvalues, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      assertTrue(it.hasNext());
      assertNull(it.next().get("col1"));
    }
  }

  @Test
  public void iterator_tsv() throws IOException {
    try (CsvRepository tsvRepository =
        new CsvRepository(testtsv, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = tsvRepository.iterator();
      Entity entity = it.next();
      assertEquals(entity.get("col1"), "val1");
      assertEquals(entity.get("col2"), "val2");
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iterator_emptylines() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptylines, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      Entity entity = it.next();
      assertEquals(entity.get("col1"), "val1");
      assertEquals(entity.get("col2"), "val2");
      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iterator_emptylines_singlecol() throws IOException {
    try (CsvRepository csvRepository =
        new CsvRepository(emptylinessinglecol, entityTypeFactory, attrMetaFactory, null)) {
      Iterator<Entity> it = csvRepository.iterator();
      Entity entity = it.next();
      assertEquals(entity.get("col1"), "val1");

      assertTrue(it.hasNext());
      entity = it.next();
      assertNull(entity.get("col1"));

      assertFalse(it.hasNext());
    }
  }

  @Test
  public void iteratorCaseSensitity() throws IOException {
    File csvFile = ResourceUtils.getFile("case-sensitivity.csv");
    try (CsvRepository csvRepository =
        new CsvRepository(csvFile, entityTypeFactory, attrMetaFactory, null)) {
      Entity entity = csvRepository.iterator().next();
      assertEquals(entity.get("Header"), "Value #0");
      assertNull(entity.get("hEADER"));
    }
  }
}
