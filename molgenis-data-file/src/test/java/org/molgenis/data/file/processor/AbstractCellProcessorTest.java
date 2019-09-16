package org.molgenis.data.file.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.file.processor.AbstractCellProcessor.processCell;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractCellProcessorTest {
  private List<CellProcessor> processors;

  @BeforeEach
  void setUp() {
    CellProcessor headerProcessor = mock(CellProcessor.class);
    when(headerProcessor.processHeader()).thenReturn(true);
    when(headerProcessor.process("col")).thenReturn("COL");

    CellProcessor dataProcessor = mock(CellProcessor.class);
    when(dataProcessor.processData()).thenReturn(true);
    when(dataProcessor.process("val")).thenReturn("VAL");

    this.processors = Arrays.asList(headerProcessor, dataProcessor);
  }

  @Test
  void processCell_null() {
    assertEquals("val", processCell("val", false, null));
  }

  @Test
  void processCell_header() {
    assertEquals("COL", processCell("col", true, processors));
  }

  @Test
  void processCell_data() {
    assertEquals("VAL", processCell("val", false, processors));
  }
}
