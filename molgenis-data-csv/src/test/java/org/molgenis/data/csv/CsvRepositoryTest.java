package org.molgenis.data.csv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.processor.CellProcessor;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

public class CsvRepositoryTest
{
	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void CsvRepository()
	{
		new CsvRepository((Reader) null, null, null);
	}

	@Test
	public void addCellProcessor_header() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();
		CsvRepository csvRepository = new CsvRepository(new StringReader("col1,col2\nval1,val2"), ',', "test", null);
		try
		{
			csvRepository.addCellProcessor(processor);
			for (@SuppressWarnings("unused")
			Entity entity : csvRepository)
			{
			}
			verify(processor).process("col1");
			verify(processor).process("col2");
		}
		finally
		{
			csvRepository.close();
		}
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		CsvRepository csvRepository = new CsvRepository(new StringReader("col1,col2\nval1,val2"), ',', "test", null);
		try
		{
			csvRepository.addCellProcessor(processor);
			for (@SuppressWarnings("unused")
			Entity entity : csvRepository)
			{
			}
			verify(processor).process("val1");
			verify(processor).process("val2");
		}
		finally
		{
			csvRepository.close();
		}
	}

	@Test
	public void metaData() throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/testdata.csv");
		File file = new File(FileUtils.getTempDirectory(), "testdata.csv");
		CsvRepository csvRepository = null;
		try
		{

			FileCopyUtils.copy(in, new FileOutputStream(file));
			csvRepository = new CsvRepository(file, null);
			assertEquals(csvRepository.getName(), "testdata");
			Iterator<AttributeMetaData> it = csvRepository.getAttributes().iterator();
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col1");
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col2");
			assertFalse(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(csvRepository);
			FileUtils.deleteQuietly(file);
		}
	}

	/**
	 * Test based on au.com.bytecode.opencsv.CSVReaderTest
	 * 
	 * @throws IOException
	 */
	@Test
	public void iterator() throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/testdata.csv");
		File file = new File(FileUtils.getTempDirectory(), "testdata.csv");
		CsvRepository csvRepository = null;
		try
		{

			FileCopyUtils.copy(in, new FileOutputStream(file));
			csvRepository = new CsvRepository(file, null);
			Iterator<Entity> it = csvRepository.iterator();

			assertTrue(it.hasNext());
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");
			assertEquals(entity.get("col2"), "val2");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), "a,a");
			assertEquals(entity.get("col2"), "b");
			assertTrue(it.hasNext());

			assertTrue(it.hasNext());
			entity = it.next();
			assertNull(entity.get("col1"));
			assertEquals(entity.get("col2"), "a");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), "\"");
			assertEquals(entity.get("col2"), "\"\"");

			assertTrue(it.hasNext());
			entity = it.next();
			assertEquals(entity.get("col1"), ",");
			assertEquals(entity.get("col2"), ",,");

			assertFalse(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(csvRepository);
			FileUtils.deleteQuietly(file);
		}
	}

	@Test
	public void iterator_noValues() throws IOException
	{
		String csvString = "col1,col2,col3";
		CsvRepository csvRepository = new CsvRepository(new StringReader(csvString), "test", null);
		try
		{
			Iterator<Entity> it = csvRepository.iterator();
			assertFalse(it.hasNext());
		}
		finally
		{
			csvRepository.close();
		}
	}

	@Test
	public void iterator_emptyValues() throws IOException
	{
		String csvString = "col1,col2,col3\n,,\n";
		CsvRepository csvRepository = new CsvRepository(new StringReader(csvString), "test", null);
		try
		{
			Iterator<Entity> it = csvRepository.iterator();
			assertTrue(it.hasNext());
			assertNull(it.next().get("col1"));
		}
		finally
		{
			csvRepository.close();
		}
	}

	@Test
	public void iterator_separator() throws IOException
	{
		CsvRepository tsvRepository = new CsvRepository(new StringReader("col1\tcol2\nval1\tval2\n"), '\t', "test",
				null);
		try
		{
			Iterator<Entity> it = tsvRepository.iterator();
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");
			assertEquals(entity.get("col2"), "val2");
			assertFalse(it.hasNext());
		}
		finally
		{
			tsvRepository.close();
		}
	}

	@Test
	public void close() throws IOException
	{
		Reader reader = mock(Reader.class);
		CsvRepository csvRepository = new CsvRepository(reader, "test", null);
		csvRepository.close();
		verify(reader).close();
	}
}
