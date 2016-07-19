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
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CsvWriterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	private EntityMetaData entityMeta;

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CsvWriter()
	{
		new CsvWriter((Writer) null);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = entityMetaFactory.create();
		entityMeta.addAttribute(attrMetaFactory.create().setName("col1"));
		entityMeta.addAttribute(attrMetaFactory.create().setName("col2"));
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

		Entity entity = new DynamicEntity(entityMeta);
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
			Entity entity = new DynamicEntity(entityMeta);
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
	public void testLabels() throws IOException
	{
		StringWriter strWriter = new StringWriter();
		CsvWriter csvWriter = new CsvWriter(strWriter);
		try
		{
			csvWriter.writeAttributes(Arrays.asList("col1", "col2"), Arrays.asList("label1", "label2"));
			Entity entity = new DynamicEntity(entityMeta);
			entity.set("col1", "val1");
			entity.set("col2", "val2");
			csvWriter.add(entity);
			assertEquals(strWriter.toString(), "\"label1\",\"label2\"\n\"val1\",\"val2\"\n");
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
