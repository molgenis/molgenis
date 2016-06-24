package org.molgenis.data.csv;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.Entity;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class CsvIteratorTest
{
	@Test
	public void testIterator() throws FileNotFoundException, IOException
	{
		InputStream in = getClass().getResourceAsStream("/testdata.csv");
		File csvFile = new File(FileUtils.getTempDirectory(), "testdata.csv");
		FileCopyUtils.copy(in, new FileOutputStream(csvFile));

		CsvIterator it = new CsvIterator(csvFile, "testdata", null, null);
		assertEquals(it.getColNamesMap().keySet(), Sets.newLinkedHashSet(Arrays.asList("col1", "col2")));
		assertEquals(Iterators.size(it), 5);

		it = new CsvIterator(csvFile, "testdata", null, null);
		Entity entity = it.next();
		assertEquals(entity.get("col1"), "val1");
		assertEquals(entity.get("col2"), "val2");
	}

}
