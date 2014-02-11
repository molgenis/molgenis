package org.molgenis.data.excel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.Repository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExcelRepositorySourceTest
{
	private InputStream is;
	private ExcelRepositorySource excelRepositorySource;

	@BeforeMethod
	public void beforeMethod() throws InvalidFormatException, IOException
	{
		is = getClass().getResourceAsStream("/test.xls");
		excelRepositorySource = new ExcelRepositorySource("test.xls", is);
	}

	@AfterMethod
	public void afterMethod()
	{
		IOUtils.closeQuietly(is);
	}

	@Test
	public void getNumberOfSheets()
	{
		assertEquals(excelRepositorySource.getNumberOfSheets(), 2);
	}

	@Test
	public void getRepositories()
	{
		List<Repository> repositories = excelRepositorySource.getRepositories();
		assertNotNull(repositories);
		assertEquals(repositories.size(), 2);
	}

	@Test
	public void getRepository()
	{
		Repository test = excelRepositorySource.getRepository("test");
		assertNotNull(test);
		assertEquals(test.getName(), "test");

		Repository blad2 = excelRepositorySource.getRepository("Blad2");
		assertNotNull(blad2);
		assertEquals(blad2.getName(), "Blad2");
	}
}
