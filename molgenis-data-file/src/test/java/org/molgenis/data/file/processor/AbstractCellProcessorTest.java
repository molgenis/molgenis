package org.molgenis.data.file.processor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class AbstractCellProcessorTest
{
	private List<CellProcessor> processors;

	@BeforeMethod
	public void setUp()
	{
		CellProcessor headerProcessor = mock(CellProcessor.class);
		when(headerProcessor.processHeader()).thenReturn(true);
		when(headerProcessor.process("col")).thenReturn("COL");

		CellProcessor dataProcessor = mock(CellProcessor.class);
		when(dataProcessor.processData()).thenReturn(true);
		when(dataProcessor.process("val")).thenReturn("VAL");

		this.processors = Arrays.asList(headerProcessor, dataProcessor);
	}

	@Test
	public void processCell_null()
	{
		assertEquals(AbstractCellProcessor.processCell("val", false, null), "val");
	}

	@Test
	public void processCell_header()
	{
		assertEquals(AbstractCellProcessor.processCell("col", true, processors), "COL");
	}

	@Test
	public void processCell_data()
	{
		assertEquals(AbstractCellProcessor.processCell("val", false, processors), "VAL");
	}
}
