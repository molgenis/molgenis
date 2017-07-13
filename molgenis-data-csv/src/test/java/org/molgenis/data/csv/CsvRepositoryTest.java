package org.molgenis.data.csv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.processor.CellProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class CsvRepositoryTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private static File test;
	private static File testdata;
	private static File novalues;
	private static File emptyvalues;
	private static File testtsv;
	private static File emptylines;
	private static File emptylinessinglecol;

	@BeforeClass
	public static void beforeClass() throws IOException
	{
		InputStream in = CsvRepositoryTest.class.getResourceAsStream("/test.csv");
		test = new File(FileUtils.getTempDirectory(), "test.csv");
		FileCopyUtils.copy(in, new FileOutputStream(test));

		in = CsvRepositoryTest.class.getResourceAsStream("/testdata.csv");
		testdata = new File(FileUtils.getTempDirectory(), "testdata.csv");
		FileCopyUtils.copy(in, new FileOutputStream(testdata));

		in = CsvRepositoryTest.class.getResourceAsStream("/novalues.csv");
		novalues = new File(FileUtils.getTempDirectory(), "novalues.csv");
		FileCopyUtils.copy(in, new FileOutputStream(novalues));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptyvalues.csv");
		emptyvalues = new File(FileUtils.getTempDirectory(), "emptyvalues.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptyvalues));

		in = CsvRepositoryTest.class.getResourceAsStream("/test.tsv");
		testtsv = new File(FileUtils.getTempDirectory(), "test.tsv");
		FileCopyUtils.copy(in, new FileOutputStream(testtsv));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptylines.csv");
		emptylines = new File(FileUtils.getTempDirectory(), "emptylines.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptylines));

		in = CsvRepositoryTest.class.getResourceAsStream("/emptylinessinglecol.csv");
		emptylinessinglecol = new File(FileUtils.getTempDirectory(), "emptylinessinglecol.csv");
		FileCopyUtils.copy(in, new FileOutputStream(emptylinessinglecol));
	}

	@Test
	public void addCellProcessor_header() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processHeader()).thenReturn(true).getMock();
		when(processor.process("col1")).thenReturn("col1");
		when(processor.process("col2")).thenReturn("col2");

		try (CsvRepository csvRepository = new CsvRepository(test, entityTypeFactory, attrMetaFactory, null))
		{
			csvRepository.addCellProcessor(processor);
			for (@SuppressWarnings("unused") Entity entity : csvRepository)
			{
			}
			verify(processor).process("col1");
			verify(processor).process("col2");
		}
	}

	@Test
	public void addCellProcessor_data() throws IOException
	{
		CellProcessor processor = when(mock(CellProcessor.class).processData()).thenReturn(true).getMock();
		try (CsvRepository csvRepository = new CsvRepository(test, entityTypeFactory, attrMetaFactory, null))
		{
			csvRepository.addCellProcessor(processor);
			for (@SuppressWarnings("unused") Entity entity : csvRepository)
			{
			}
			verify(processor).process("val1");
			verify(processor).process("val2");
		}
	}

	@Test
	public void metaData() throws IOException
	{
		CsvRepository csvRepository = null;
		try
		{
			csvRepository = new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null);
			assertEquals(csvRepository.getName(), "testdata");
			Iterator<Attribute> it = csvRepository.getEntityType().getAttributes().iterator();
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col1");
			assertTrue(it.hasNext());
			assertEquals(it.next().getName(), "col2");
			assertFalse(it.hasNext());
		}
		finally
		{
			IOUtils.closeQuietly(csvRepository);
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
		CsvRepository csvRepository = null;
		try
		{
			csvRepository = new CsvRepository(testdata, entityTypeFactory, attrMetaFactory, null);
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
		}
	}

	@Test
	public void iterator_noValues() throws IOException
	{
		try (CsvRepository csvRepository = new CsvRepository(novalues, entityTypeFactory, attrMetaFactory, null))
		{
			Iterator<Entity> it = csvRepository.iterator();
			assertFalse(it.hasNext());
		}
	}

	@Test
	public void iterator_emptyValues() throws IOException
	{
		try (CsvRepository csvRepository = new CsvRepository(emptyvalues, entityTypeFactory, attrMetaFactory, null))
		{
			Iterator<Entity> it = csvRepository.iterator();
			assertTrue(it.hasNext());
			assertNull(it.next().get("col1"));
		}
	}

	@Test
	public void iterator_tsv() throws IOException
	{
		try (CsvRepository tsvRepository = new CsvRepository(testtsv, entityTypeFactory, attrMetaFactory, null))
		{
			Iterator<Entity> it = tsvRepository.iterator();
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");
			assertEquals(entity.get("col2"), "val2");
			assertFalse(it.hasNext());
		}
	}

	@Test
	public void iterator_emptylines() throws IOException
	{
		try (CsvRepository csvRepository = new CsvRepository(emptylines, entityTypeFactory, attrMetaFactory, null))
		{
			Iterator<Entity> it = csvRepository.iterator();
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");
			assertEquals(entity.get("col2"), "val2");
			assertFalse(it.hasNext());
		}
	}

	@Test
	public void iterator_emptylines_singlecol() throws IOException
	{
		try (CsvRepository csvRepository = new CsvRepository(emptylinessinglecol, entityTypeFactory, attrMetaFactory,
				null))
		{
			Iterator<Entity> it = csvRepository.iterator();
			Entity entity = it.next();
			assertEquals(entity.get("col1"), "val1");

			assertTrue(it.hasNext());
			entity = it.next();
			assertNull(entity.get("col1"));

			assertFalse(it.hasNext());
		}
	}
}
