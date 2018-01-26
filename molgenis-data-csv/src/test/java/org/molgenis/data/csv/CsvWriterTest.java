package org.molgenis.data.csv;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class CsvWriterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private EntityType entityType;

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CsvWriter()
	{
		new CsvWriter((Writer) null);
	}

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = entityTypeFactory.create();
		entityType.addAttribute(attrMetaFactory.create().setName("col1"));
		entityType.addAttribute(attrMetaFactory.create().setName("col2"));
	}

	@Test
	public void addCellProcessor() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();

		try (CsvWriter csvWriter = new CsvWriter(new StringWriter()))
		{
			csvWriter.addCellProcessor(processor);
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();

		Entity entity = new DynamicEntity(entityType);
		entity.set("col1", "val1");
		entity.set("col2", "val2");

		try (CsvWriter csvWriter = new CsvWriter(new StringWriter()))
		{
			csvWriter.addCellProcessor(processor);
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
			csvWriter.add(entity);
		}
		verify(processor).process("val1");
		verify(processor).process("val2");
	}

	@Test
	public void add() throws IOException
	{
		StringWriter strWriter = new StringWriter();
		try (CsvWriter csvWriter = new CsvWriter(strWriter))
		{
			csvWriter.writeAttributeNames(Arrays.asList("col1", "col2"));
			Entity entity = new DynamicEntity(entityType);
			entity.set("col1", "val1");
			entity.set("col2", "val2");
			csvWriter.add(entity);
			assertEquals(strWriter.toString(), "\"col1\",\"col2\"\n\"val1\",\"val2\"\n");
		}
	}

	@Test
	public void testLabels() throws IOException
	{
		StringWriter strWriter = new StringWriter();
		try (CsvWriter csvWriter = new CsvWriter(strWriter))
		{
			csvWriter.writeAttributes(Arrays.asList("col1", "col2"), Arrays.asList("label1", "label2"));
			Entity entity = new DynamicEntity(entityType);
			entity.set("col1", "val1");
			entity.set("col2", "val2");
			csvWriter.add(entity);
			assertEquals(strWriter.toString(), "\"label1\",\"label2\"\n\"val1\",\"val2\"\n");
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
