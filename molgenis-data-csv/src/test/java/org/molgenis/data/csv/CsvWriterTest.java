package org.molgenis.data.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;

class CsvWriterTest extends AbstractMolgenisSpringTest {
  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attrMetaFactory;

  private EntityType entityType;

  @SuppressWarnings("resource")
  @Test
  void CsvWriter() {
    assertThrows(IllegalArgumentException.class, () -> new CsvWriter((Writer) null));
  }

  @BeforeEach
  void setUpBeforeMethod() {
    entityType = entityTypeFactory.create();
    entityType.addAttribute(attrMetaFactory.create().setName("col1"));
    entityType.addAttribute(attrMetaFactory.create().setName("col2"));
  }

  @Test
  void addCellProcessor() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();

    try (CsvWriter csvWriter = new CsvWriter(new StringWriter())) {
      csvWriter.addCellProcessor(processor);
      csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
    }
    verify(processor).process("col1");
    verify(processor).process("col2");
  }

  @Test
  void addCellProcessor_data() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();

    Entity entity = new DynamicEntity(entityType);
    entity.set("col1", "val1");
    entity.set("col2", "val2");

    try (CsvWriter csvWriter = new CsvWriter(new StringWriter())) {
      csvWriter.addCellProcessor(processor);
      csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
      csvWriter.add(entity);
    }
    verify(processor).process("val1");
    verify(processor).process("val2");
  }

  @Test
  void add() throws IOException {
    StringWriter strWriter = new StringWriter();
    try (CsvWriter csvWriter = new CsvWriter(strWriter)) {
      csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
      Entity entity = new DynamicEntity(entityType);
      entity.set("col1", "val1");
      entity.set("col2", "val2");
      csvWriter.add(entity);
      assertEquals("\"col1\",\"col2\"\n\"val1\",\"val2\"\n", strWriter.toString());
    }
  }

  @Test
  void testLabels() throws IOException {
    StringWriter strWriter = new StringWriter();
    try (CsvWriter csvWriter = new CsvWriter(strWriter)) {
      csvWriter.writeAttributes(Arrays.asList("col1", "col2"), Arrays.asList("label1", "label2"));
      Entity entity = new DynamicEntity(entityType);
      entity.set("col1", "val1");
      entity.set("col2", "val2");
      csvWriter.add(entity);
      assertEquals("\"label1\",\"label2\"\n\"val1\",\"val2\"\n", strWriter.toString());
    }
  }
}
