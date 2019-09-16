package org.molgenis.data.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;

class ExcelWriterTest extends AbstractMolgenisSpringTest {
  @Autowired private AttributeFactory attrMetaFactory;

  @SuppressWarnings("resource")
  @Test
  void ExcelWriter() {
    assertThrows(
        NullPointerException.class, () -> new ExcelWriter((OutputStream) null, attrMetaFactory));
  }

  @Test
  void ExcelWriterFileFormat_default() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ExcelWriter(bos, attrMetaFactory).close();
    byte[] b = bos.toByteArray();
    assertEquals(0xD0, b[0] & 0xff);
    assertEquals(0xCF, b[1] & 0xff);
    assertEquals(0x11, b[2] & 0xff);
    assertEquals(0xE0, b[3] & 0xff);
  }

  @Test
  void ExcelWriterFileFormat_XLS() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ExcelWriter(bos, attrMetaFactory, FileFormat.XLS).close();
    byte[] b = bos.toByteArray();
    assertEquals(0xD0, b[0] & 0xff);
    assertEquals(0xCF, b[1] & 0xff);
    assertEquals(0x11, b[2] & 0xff);
    assertEquals(0xE0, b[3] & 0xff);
  }

  @Test
  void ExcelWriterFileFormat_XLSX() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ExcelWriter(bos, attrMetaFactory, FileFormat.XLSX).close();
    byte[] b = bos.toByteArray();
    assertEquals(0x50, b[0] & 0xff);
    assertEquals(0x4B, b[1] & 0xff);
    assertEquals(0x03, b[2] & 0xff);
    assertEquals(0x04, b[3] & 0xff);
  }

  @Test
  void addCellProcessor_header() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();

    OutputStream os = mock(OutputStream.class);
    ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
    excelWriter.addCellProcessor(processor);
    try {
      excelWriter.createWritable("sheet", Arrays.asList("col1", "col2"));
    } finally {
      excelWriter.close();
    }
    verify(processor).process("col1");
    verify(processor).process("col2");
  }

  @Test
  void addCellProcessor_data() throws IOException {
    CellProcessor processor =
        when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
    OutputStream os = mock(OutputStream.class);
    ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
    excelWriter.addCellProcessor(processor);
    try {
      Entity entity =
          new DynamicEntity(mock(EntityType.class)) {
            @Override
            protected void validateValueType(String attrName, Object value) {
              // noop
            }
          };
      entity.set("col1", "val1");
      entity.set("col2", "val2");
      Writable writable = excelWriter.createWritable("test", Arrays.asList("col1", "col2"));
      writable.add(entity);
    } finally {
      excelWriter.close();
    }
    verify(processor).process("val1");
    verify(processor).process("val2");
  }

  @Test
  void close() throws IOException {
    OutputStream os = mock(OutputStream.class);
    ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
    excelWriter.close();
    verify(os).close();
  }

  @Test
  void createSheet() throws IOException {
    OutputStream os = mock(OutputStream.class);
    try (ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory)) {
      assertNotNull(excelWriter.createWritable("sheet", null));
    }
  }

  @Test
  void createSheet_null() throws IOException {
    OutputStream os = mock(OutputStream.class);
    try (ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory)) {
      assertThrows(IllegalArgumentException.class, () -> excelWriter.createWritable(null, null));
    }
  }
}
