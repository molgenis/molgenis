package org.molgenis.data.excel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class ExcelRepositorySourceTest
{
	private InputStream is;
	private ExcelRepositoryCollection excelRepositoryCollection;

	@BeforeMethod
	public void beforeMethod() throws MolgenisInvalidFormatException, IOException
	{
		is = getClass().getResourceAsStream("/test.xls");
		excelRepositoryCollection = new ExcelRepositoryCollection("test.xls", is);
	}

	@AfterMethod
	public void afterMethod()
	{
		IOUtils.closeQuietly(is);
	}

	@Test
	public void getNumberOfSheets()
	{
		assertEquals(excelRepositoryCollection.getNumberOfSheets(), 3);
	}

	@Test
	public void getRepositories()
	{
		List<String> repositories = Lists.newArrayList(excelRepositoryCollection.getEntityNames());
		assertNotNull(repositories);
		assertEquals(repositories.size(), 3);
	}

	@Test
	public void getRepository()
	{
		Repository test = excelRepositoryCollection.getRepository("test");
		assertNotNull(test);
		assertEquals(test.getName(), "test");

		Repository blad2 = excelRepositoryCollection.getRepository("Blad2");
		assertNotNull(blad2);
		assertEquals(blad2.getName(), "Blad2");
	}
}
