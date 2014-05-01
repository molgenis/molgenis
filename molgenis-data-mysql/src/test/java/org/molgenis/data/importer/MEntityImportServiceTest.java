package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class MEntityImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection store;

	@Test
	public void test1() throws IOException, InvalidFormatException, InterruptedException
	{
        //cleanup
        store.drop("import_person");
        store.drop("import_city");
        store.drop("import_country");

        // create test excel
		File f = new File("/Users/mswertz/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepositoryByEntityName("attributes"));

		List<Entity> entities = new ArrayList<Entity>();
		for (Entity e : source.getRepositoryByEntityName("attributes"))
		{
			System.out.println(e);
		}

		MEntityImportServiceImpl importer = new MEntityImportServiceImpl();
		importer.setRepositoryCollection(store);

		for (EntityMetaData em : importer.getEntityMetaData(source).values())
		{
			System.out.println(em);
		}

		// test import
		importer.doImport(source, null);

        //query
        for(Entity e: store.getRepositoryByEntityName("import_city"))
        {
            System.out.println(e);
        }

        for(Entity e: store.getRepositoryByEntityName("import_person"))
        {
            System.out.println(e);
        }

		// wait to make sure logger has outputted
		Thread.sleep(1000);

	}
}
