package org.molgenis.data.excel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.molgenis.data.Entity;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelWriter.FileFormat;
import org.molgenis.data.support.MapEntity;
import org.molgenis.io.processor.CellProcessor;
import org.testng.annotations.Test;

public class ExcelWriterTest
{
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void ExcelWriter()
	{
		new ExcelWriter((OutputStream) null);
	}

	@Test
	public void ExcelWriterFileFormat_default() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new ExcelWriter(bos).close();
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
		new ExcelWriter(bos, FileFormat.XLS).close();
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
		new ExcelWriter(bos, FileFormat.XLSX).close();
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
		ExcelWriter excelWriter = new ExcelWriter(os);
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
		ExcelWriter excelWriter = new ExcelWriter(os);
		excelWriter.addCellProcessor(processor);
		try
		{
			Entity entity = new MapEntity();
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
		ExcelWriter excelWriter = new ExcelWriter(os);
		excelWriter.close();
		verify(os).close();
	}

	@Test
	public void createSheet() throws IOException
	{
		OutputStream os = mock(OutputStream.class);
		ExcelWriter excelWriter = new ExcelWriter(os);
		try
		{
			assertNotNull(excelWriter.createWritable("sheet", null));
		}
		finally
		{
			excelWriter.close();
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void createSheet_null() throws IOException
	{
		OutputStream os = mock(OutputStream.class);
		ExcelWriter excelWriter = new ExcelWriter(os);
		try
		{
			assertNotNull(excelWriter.createWritable(null, null));
		}
		finally
		{
			excelWriter.close();
		}
	}
}
