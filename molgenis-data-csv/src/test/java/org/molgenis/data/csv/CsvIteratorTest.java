package org.molgenis.data.csv;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;

public class CsvIteratorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private EntityType entityType;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityType = entityTypeFactory.create();
		entityType.addAttribute(attrMetaFactory.create().setName("col1"));
		entityType.addAttribute(attrMetaFactory.create().setName("col2"));
	}

	@Test
	public void testIteratorFromCsvFile() throws IOException
	{
		File csvFile = createTmpFileForResource("testdata.csv");

		CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);

		it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertEquals(entity.get("col2"), "val2");
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Number of values \\(1\\) doesn't match the number of headers \\(2\\): \\[val1\\]")
	public void testIteratorValueHeaderMismatchOneNonEmptyValue() throws IOException
	{
		File csvFile = File.createTempFile("testdata", ".csv");
		try
		{
			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile)))
			{
				outputStreamWriter.write("col1,col2\n");
				outputStreamWriter.write("val1\n");
			}
			new CsvIterator(csvFile, "testdata", null, ',', entityType).next();
		}
		finally
		{
			//noinspection ResultOfMethodCallIgnored
			csvFile.delete();
		}
	}

	@SuppressWarnings("deprecation")
	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Number of values \\(2\\) doesn't match the number of headers \\(3\\): \\[val1,val2\\]")
	public void testIteratorValueHeaderMismatch() throws IOException
	{
		File csvFile = File.createTempFile("testdata", ".csv");
		try
		{
			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile)))
			{
				outputStreamWriter.write("col1,col2,col3\n");
				outputStreamWriter.write("val1,val2\n");
			}
			new CsvIterator(csvFile, "testdata", null, ',', entityType).next();
		}
		finally
		{
			//noinspection ResultOfMethodCallIgnored
			csvFile.delete();
		}
	}

	@Test
	public void testIteratorFromZipFile() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFile.zip");

		CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	@Test
	public void testIteratorFromZipFileWithFolder() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithFolder.zip");

		CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	@Test
	public void testIteratorFromCsvFileWithBom() throws IOException
	{
		File csvFile = createTmpFileForResource("testDataWithBom.csv");

		CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);

		it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertEquals(entity.get("col2"), "val2");
	}

	@Test
	public void testIteratorFromZipFileWithBom() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithBom.zip");

		CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	@Test
	public void testIteratorFromZipFileWithFolderWithBom() throws IOException
	{
		File zipFile = createTmpFileForResource("zipFileWithFolderWithBom.zip");

		CsvIterator it = new CsvIterator(zipFile, "testDataWithBom", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	private File createTmpFileForResource(String fileName) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + fileName);
		File csvFile = new File(FileUtils.getTempDirectory(), fileName);
		FileCopyUtils.copy(in, new FileOutputStream(csvFile));
		return csvFile;
	}
}
