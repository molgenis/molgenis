package org.molgenis.data.csv;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
		InputStream in = getClass().getResourceAsStream("/testdata.csv");
		File csvFile = new File(FileUtils.getTempDirectory(), "testdata.csv");
		FileCopyUtils.copy(in, new FileOutputStream(csvFile));

		CsvIterator it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);

		it = new CsvIterator(csvFile, "testdata", null, null, entityType);
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertEquals(entity.get("col2"), "val2");
	}

	@Test
	public void testIteratorFromZipFile() throws IOException
	{
		File zipFile = getZipWithCsvFile("testdata.csv");

		CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	@Test
	public void testIteratorFromZipFileWithFolder() throws IOException
	{
		File zipFile = getZipWithCsvFile("folder/testdata.csv");

		CsvIterator it = new CsvIterator(zipFile, "testdata", null, null, entityType);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);
	}

	private File getZipWithCsvFile(String filePath) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/testdata.csv");

		File zipFile = new File(FileUtils.getTempDirectory() + "test.zip");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		ZipEntry e = new ZipEntry(filePath);
		out.putNextEntry(e);

		IOUtils.copy(in, out);

		out.closeEntry();

		in.close();
		out.close();
		return zipFile;
	}
}
