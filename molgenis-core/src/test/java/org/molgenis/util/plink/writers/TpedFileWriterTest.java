package org.molgenis.util.plink.writers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.util.plink.datatypes.Biallele;
import org.molgenis.util.plink.datatypes.TpedEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TpedFileWriterTest
{

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void TpedFileWriter() throws IOException
	{
		TpedFileWriter fileWriter = null;
		try
		{
			fileWriter = new TpedFileWriter(null);
		}
		finally
		{
			if (fileWriter != null) fileWriter.close();
		}
	}

	@Test
	public void writeTpedEntry() throws IOException
	{
		File file0 = File.createTempFile("TpedFileWriterTest_file0", null);
		try
		{
			TpedFileWriter fileWriter = null;
			try
			{
				fileWriter = new TpedFileWriter(file0);
				Biallele b1 = new Biallele('A', 'A');
				Biallele b2 = new Biallele('A', 'C');
				Biallele b3 = new Biallele('C', 'C');
				Biallele b4 = new Biallele('A', 'C');
				Biallele b5 = new Biallele('C', 'C');
				Biallele b6 = new Biallele('C', 'C');
				fileWriter.write(new TpedEntry("1", "snp1", 0.0, 5000650, Arrays.asList(b1, b2, b3, b4, b5, b6)));
			}
			finally
			{
				IOUtils.closeQuietly(fileWriter);
			}

			String expected = "1 snp1 0.0 5000650 A A A C C C A C C C C C\n";
			Assert.assertEquals(FileUtils.readFileToString(file0, Charset.forName("UTF-8")), expected);
		}
		finally
		{
			file0.delete();
		}
	}

	@Test
	public void writeIterableTpedEntry() throws IOException
	{
		List<TpedEntry> entryList = new ArrayList<TpedEntry>();
		Biallele b0_1 = new Biallele('A', 'A');
		Biallele b0_2 = new Biallele('A', 'C');
		Biallele b0_3 = new Biallele('C', 'C');
		Biallele b0_4 = new Biallele('A', 'C');
		Biallele b0_5 = new Biallele('C', 'C');
		Biallele b0_6 = new Biallele('C', 'C');
		entryList.add(new TpedEntry("1", "snp1", 0.0, 5000650, Arrays.asList(b0_1, b0_2, b0_3, b0_4, b0_5, b0_6)));
		Biallele b1_1 = new Biallele('G', 'T');
		Biallele b1_2 = new Biallele('G', 'T');
		Biallele b1_3 = new Biallele('G', 'G');
		Biallele b1_4 = new Biallele('T', 'T');
		Biallele b1_5 = new Biallele('G', 'T');
		Biallele b1_6 = new Biallele('T', 'T');
		entryList.add(new TpedEntry("1", "snp2", 0.0, 5000830, Arrays.asList(b1_1, b1_2, b1_3, b1_4, b1_5, b1_6)));

		File file0 = File.createTempFile("TpedFileWriterTest_file0", null);
		try
		{
			TpedFileWriter fileWriter = null;
			try
			{
				fileWriter = new TpedFileWriter(file0);
				fileWriter.write(entryList);
			}
			finally
			{
				IOUtils.closeQuietly(fileWriter);
			}

			String expected = "1 snp1 0.0 5000650 A A A C C C A C C C C C\n1 snp2 0.0 5000830 G T G T G G T T G T T T\n";
			Assert.assertEquals(FileUtils.readFileToString(file0, Charset.forName("UTF-8")), expected);
		}
		finally
		{
			file0.delete();
		}
	}
}
