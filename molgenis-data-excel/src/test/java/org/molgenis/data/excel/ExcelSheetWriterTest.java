package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;

class ExcelSheetWriterTest extends AbstractMolgenisSpringTest {
  @Autowired private AttributeFactory attrMetaFactory;

  private ExcelWriter excelWriter;
  private ByteArrayOutputStream bos;
  private ExcelSheetWriter excelSheetWriter;

  @BeforeEach
  void setUp() {
    bos = new ByteArrayOutputStream();
    excelWriter = new ExcelWriter(bos, attrMetaFactory);
    excelSheetWriter = excelWriter.createWritable("sheet", Arrays.asList("col1", "col2"));
  }

  @AfterEach
  void tearDown() throws IOException {
    excelWriter.close();
  }

  @Test
  void addCellProcessor() {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();

    Entity entity =
        new DynamicEntity(mock(EntityType.class)) {
          @Override
          protected void validateValueType(String attrName, Object value) {
            // noop
          }
        };
    entity.set("col1", "val1");
    entity.set("col2", "val2");

    excelSheetWriter.addCellProcessor(processor);
    excelSheetWriter.add(entity);

    verify(processor).process("val1");
    verify(processor).process("val2");
  }

  @Test
  void write() throws IOException {
    Entity entity1 =
        new DynamicEntity(mock(EntityType.class)) {
          @Override
          protected void validateValueType(String attrName, Object value) {
            // noop
          }
        };
    entity1.set("col1", "val1");
    entity1.set("col2", "val2");
    excelSheetWriter.add(entity1);

    Entity entity2 =
        new DynamicEntity(mock(EntityType.class)) {
          @Override
          protected void validateValueType(String attrName, Object value) {
            // noop
          }
        };
    entity2.set("col1", "val3");
    entity2.set("col2", "val4");
    excelSheetWriter.add(entity2);

    excelWriter.close();
  }
}
