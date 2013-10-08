package org.molgenis.data.excel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.EntitySource;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

public class ExcelEntitySourceFactoryTest
{
	@Test
	public void create() throws FileNotFoundException, IOException
	{
		InputStream in = null;
		File xlsFile = null;
		try
		{
			in = getClass().getResourceAsStream("/test.xls");
			xlsFile = File.createTempFile("molgenis", ".xls");
			FileCopyUtils.copy(in, new FileOutputStream(xlsFile));
			String url = "excel://" + xlsFile.getAbsolutePath();
			EntitySource entitySource = new ExcelEntitySourceFactory().create(url);
			assertNotNull(entitySource);
			assertTrue(entitySource instanceof ExcelEntitySource);
			assertEquals(entitySource.getUrl(), url);
		}
		finally
		{
			IOUtils.closeQuietly(in);
			if (xlsFile != null)
			{
				xlsFile.delete();
			}
		}
	}

	@Test
	public void getUrlPrefix()
	{
		assertNotNull(new ExcelEntitySourceFactory().getUrlPrefix());
	}
}
