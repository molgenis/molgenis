package org.molgenis.data.excel;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ExcelRepositorySourceTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeFactory attrMetaFactory;

	private InputStream is;
	private ExcelRepositoryCollection excelRepositoryCollection;

	@BeforeMethod
	public void beforeMethod() throws MolgenisInvalidFormatException, IOException
	{
		is = getClass().getResourceAsStream("/test.xls");
		excelRepositoryCollection = new ExcelRepositoryCollection("test.xls", is);
		excelRepositoryCollection.setEntityMetaDataFactory(entityMetaFactory);
		excelRepositoryCollection.setAttributeMetaDataFactory(attrMetaFactory);
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
		Repository<Entity> test = excelRepositoryCollection.getRepository("test");
		assertNotNull(test);
		assertEquals(test.getName(), "test");

		Repository<Entity> blad2 = excelRepositoryCollection.getRepository("Blad2");
		assertNotNull(blad2);
		assertEquals(blad2.getName(), "Blad2");
	}
}
