package org.molgenis.data.excel;

import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.file.processor.CellProcessor;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ExcelWriterTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AttributeFactory attrMetaFactory;

	@SuppressWarnings("resource")
	@Test(expectedExceptions = NullPointerException.class)
	public void ExcelWriter()
	{
		new ExcelWriter((OutputStream) null, attrMetaFactory);
	}

	@Test
	public void ExcelWriterFileFormat_default() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ExcelWriter(bos, attrMetaFactory).close();
		byte[] b = bos.toByteArray();
		assertEquals(b[0] & 0xff, 0xD0);
		assertEquals(b[1] & 0xff, 0xCF);
		assertEquals(b[2] & 0xff, 0x11);
		assertEquals(b[3] & 0xff, 0xE0);
	}

	@Test
	public void ExcelWriterFileFormat_XLS() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ExcelWriter(bos, attrMetaFactory, FileFormat.XLS).close();
		byte[] b = bos.toByteArray();
		assertEquals(b[0] & 0xff, 0xD0);
		assertEquals(b[1] & 0xff, 0xCF);
		assertEquals(b[2] & 0xff, 0x11);
		assertEquals(b[3] & 0xff, 0xE0);
	}

	@Test
	public void ExcelWriterFileFormat_XLSX() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ExcelWriter(bos, attrMetaFactory, FileFormat.XLSX).close();
		byte[] b = bos.toByteArray();
		assertEquals(b[0] & 0xff, 0x50);
		assertEquals(b[1] & 0xff, 0x4B);
		assertEquals(b[2] & 0xff, 0x03);
		assertEquals(b[3] & 0xff, 0x04);
	}

	@Test
	public void addCellProcessor_header() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();

		OutputStream os = mock(OutputStream.class);
		ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
		excelWriter.addCellProcessor(processor);
		try
		{
			excelWriter.createWritable("sheet", Arrays.asList("col1", "col2"));
		}
		finally
		{
			excelWriter.close();
		}
		verify(processor).process("col1");
		verify(processor).process("col2");
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		OutputStream os = mock(OutputStream.class);
		ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
		excelWriter.addCellProcessor(processor);
		try
		{
			Entity entity = new DynamicEntity(mock(EntityType.class))
			{
				@Override
				protected void validateValueType(String attrName, Object value)
				{
					// noop
				}
			};
			entity.set("col1", "val1");
			entity.set("col2", "val2");
			Writable writable = excelWriter.createWritable("test", Arrays.asList("col1", "col2"));
			writable.add(entity);
		}
		finally
		{
			excelWriter.close();
		}
		verify(processor).process("val1");
		verify(processor).process("val2");
	}

	@Test
	public void close() throws IOException
	{
		OutputStream os = mock(OutputStream.class);
		ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory);
		excelWriter.close();
		verify(os).close();
	}

	@Test
	public void createSheet() throws IOException
	{
		OutputStream os = mock(OutputStream.class);
		try (ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory))
		{
			assertNotNull(excelWriter.createWritable("sheet", null));
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createSheet_null() throws IOException
	{
		OutputStream os = mock(OutputStream.class);
		try (ExcelWriter excelWriter = new ExcelWriter(os, attrMetaFactory))
		{
			assertNotNull(excelWriter.createWritable(null, null));
		}
	}
}
