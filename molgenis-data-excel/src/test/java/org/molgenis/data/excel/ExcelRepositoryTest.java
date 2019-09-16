package org.molgenis.data.excel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;

class ExcelRepositoryTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private ExcelRepository excelSheetReader;

  private Workbook workbook;
  private InputStream is;

  ExcelRepositoryTest() {
    super(Strictness.WARN);
  }

  @BeforeEach
  void beforeMethod() throws InvalidFormatException, IOException {
    is = getClass().getResourceAsStream("/test.xls");
    workbook = WorkbookFactory.create(is);
    excelSheetReader =
        new ExcelRepository(
            "test.xls", workbook.getSheet("test"), entityTypeFactory, attrMetaFactory);
  }

  @AfterEach
  void afterMethod() throws IOException {
    is.close();
  }

  @SuppressWarnings({"resource", "deprecation"})
  @Test
  void ExcelRepository() {
    assertThrows(
        MolgenisDataException.class,
        () ->
            new ExcelRepository(
                "test.xls",
                workbook.getSheet("test_mergedcells"),
                entityTypeFactory,
                attrMetaFactory));
  }

  @Test
  void addCellProcessor_header() {
    CellProcessor processor = mock(CellProcessor.class);
    when(processor.processHeader()).thenReturn(true);
    when(processor.process("col1")).thenReturn("col1");
    when(processor.process("col2")).thenReturn("col2");

    excelSheetReader.addCellProcessor(processor);
    //noinspection StatementWithEmptyBody
    for (@SuppressWarnings("unused") Entity entity : excelSheetReader) {}
    verify(processor).process("col1");
    verify(processor).process("col2");
  }

  @Test
  void addCellProcessor_data() {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
    excelSheetReader.addCellProcessor(processor);
    for (Entity entity : excelSheetReader) entity.get("col2");

    verify(processor).process("val2");
    verify(processor).process("val4");
    verify(processor).process("val6");
  }

  @Test
  void getAttribute() {
    Attribute attr = excelSheetReader.getEntityType().getAttribute("col1");
    assertNotNull(attr);
    assertEquals(STRING, attr.getDataType());
    assertEquals("col1", attr.getName());
  }

  @Test
  void getAttributes() {
    Iterator<Attribute> it = excelSheetReader.getEntityType().getAttributes().iterator();
    assertTrue(it.hasNext());
    assertEquals("col1", it.next().getName());
    assertTrue(it.hasNext());
    assertEquals("col2", it.next().getName());
    assertFalse(it.hasNext());
  }

  @Test
  void getDescription() {
    assertNull(excelSheetReader.getEntityType().getDescription());
  }

  @Test
  void getIdAttribute() {
    assertNull(excelSheetReader.getEntityType().getIdAttribute());
  }

  @Test
  void getLabel() {
    assertEquals("test", excelSheetReader.getEntityType().getLabel());
  }

  @Test
  void getLabelAttribute() {
    assertNull(excelSheetReader.getEntityType().getLabelAttribute());
  }

  @Test
  void getName() {
    assertEquals("test", excelSheetReader.getName());
  }

  @Test
  void getNrRows() {
    assertEquals(5, excelSheetReader.getNrRows());
  }

  @Test
  void iterator() {
    Iterator<Entity> it = excelSheetReader.iterator();
    assertTrue(it.hasNext());

    Entity row1 = it.next();
    assertEquals("val1", row1.get("col1"));
    assertEquals("val2", row1.get("col2"));
    assertTrue(it.hasNext());

    Entity row2 = it.next();
    assertEquals("val3", row2.get("col1"));
    assertEquals("val4", row2.get("col2"));
    assertTrue(it.hasNext());

    Entity row3 = it.next();
    assertEquals("XXX", row3.get("col1"));
    assertEquals("val6", row3.get("col2"));
    assertTrue(it.hasNext());

    // test number cell (col1) and formula cell (col2)
    Entity row4 = it.next();
    assertEquals("1.2", row4.get("col1"));
    assertEquals("2.4", row4.get("col2"));
    assertFalse(it.hasNext());
  }

  @Test
  void iteratorNextWhenNoNext() {
    Iterator<Entity> it = excelSheetReader.iterator();
    it.next(); // 1
    it.next(); // 2
    it.next(); // 3
    it.next(); // 4
    assertThrows(NoSuchElementException.class, () -> it.next()); // does not exist
  }

  @SuppressWarnings("deprecation")
  @Test
  void iteratorDuplicateSheetHeader() throws IOException, InvalidFormatException {
    String fileName = "/duplicate-sheet-header.xlsx";
    try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
      Workbook workbook = WorkbookFactory.create(inputStream);
      ExcelRepository excelRepository =
          new ExcelRepository(
              fileName, workbook.getSheet("attributes"), entityTypeFactory, attrMetaFactory);
      Exception exception =
          assertThrows(MolgenisDataException.class, () -> excelRepository.iterator());
      assertThat(exception.getMessage())
          .containsPattern("Duplicate column header 'entity' in sheet 'attributes' not allowed");
    }
  }

  @Test
  void iteratorHeaderCaseSensitive() throws IOException, InvalidFormatException {
    String fileName = "/case-sensitivity.xlsx";
    try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
      Workbook workbook = WorkbookFactory.create(inputStream);
      ExcelRepository excelRepository =
          new ExcelRepository(
              fileName, workbook.getSheet("case-sensitivity"), entityTypeFactory, attrMetaFactory);
      Entity entity = excelRepository.iterator().next();
      assertEquals("Value #0", entity.get("Header"));
      assertNull(entity.get("hEADER"));
    }
  }

  @Test
  void attributesAndIterator() {
    Iterator<Attribute> headerIt = excelSheetReader.getEntityType().getAttributes().iterator();
    assertTrue(headerIt.hasNext());
    assertEquals("col1", headerIt.next().getName());
    assertTrue(headerIt.hasNext());
    assertEquals("col2", headerIt.next().getName());

    Iterator<Entity> it = excelSheetReader.iterator();
    assertTrue(it.hasNext());

    Entity row1 = it.next();
    assertEquals("val1", row1.get("col1"));
    assertEquals("val2", row1.get("col2"));
    assertTrue(it.hasNext());

    Entity row2 = it.next();
    assertEquals("val3", row2.get("col1"));
    assertEquals("val4", row2.get("col2"));
    assertTrue(it.hasNext());

    Entity row3 = it.next();
    assertEquals("XXX", row3.get("col1"));
    assertEquals("val6", row3.get("col2"));
    assertTrue(it.hasNext());

    // test number cell (col1) and formula cell (col2)
    Entity row4 = it.next();
    assertEquals("1.2", row4.get("col1"));
    assertEquals("2.4", row4.get("col2"));
    assertFalse(it.hasNext());
  }
}
