package org.molgenis.data.csv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

public class CsvWriterTest
{

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CsvWriter()
	{
		new CsvWriter((Writer) null);
	}

	@Test
	public void addCellProcessor() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();

		CsvWriter csvWriter = new CsvWriter(new StringWriter());
		try
		{
			csvWriter.addCellProcessor(processor);
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
		}
		finally
		{
			csvWriter.close();
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();

		Entity entity = new MapEntity();
		entity.set("col1", "val1");
		entity.set("col2", "val2");

		CsvWriter csvWriter = new CsvWriter(new StringWriter());
		try
		{
			csvWriter.addCellProcessor(processor);
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
			csvWriter.add(entity);
		}
		finally
		{
			csvWriter.close();
		}
		verify(processor).process("val1");
		verify(processor).process("val2");
	}

	@Test
	public void add() throws IOException
	{
		StringWriter strWriter = new StringWriter();
		CsvWriter csvWriter = new CsvWriter(strWriter);
		try
		{
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
			Entity entity = new MapEntity();
			entity.set("col1", "val1");
			entity.set("col2", "val2");
			csvWriter.add(entity);
			assertEquals(strWriter.toString(), "\"col1\",\"col2\"\n\"val1\",\"val2\"\n");
		}
		finally
		{
			csvWriter.close();
		}
	}

	@Test
	public void close() throws IOException
	{
		// FIXME enable when double closing bug in opencsv is fixed
		// Writer writer = mock(Writer.class);
		// CsvWriter csvWriter = new CsvWriter(writer);
		// csvWriter.close();
		// verify(writer).close();
	}
}
