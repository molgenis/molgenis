package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class EmxImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection store;

	@Test
	public void testValidationReport() throws IOException, InvalidFormatException
	{
		// open test source
        File f =  new File(getClass().getResource("/example_invalid.xlsx").getFile());
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		// create importer
		EmxImportServiceImpl importer = new EmxImportServiceImpl();
		importer.setRepositoryCollection(store);

		// generate report
		EntitiesValidationReport report = importer.validateImport(source);

		// SheetsImportable
		Assert.assertEquals(report.getSheetsImportable().size(), 4);
		Assert.assertTrue(report.getSheetsImportable().get("import_person"));
		Assert.assertTrue(report.getSheetsImportable().get("import_city"));
		Assert.assertFalse(report.getSheetsImportable().get("unknown_entity"));
		Assert.assertFalse(report.getSheetsImportable().get("unknown_fields"));

		// FieldsAvailable
		Assert.assertEquals(report.getFieldsAvailable().size(), 2);
		Assert.assertEquals(report.getFieldsAvailable().get("import_person").size(), 1);
		Assert.assertEquals(report.getFieldsAvailable().get("import_city").size(), 0);
		Assert.assertTrue(report.getFieldsAvailable().get("import_person").contains("otherAttribute"));

		// FieldsImportable
		Assert.assertEquals(report.getFieldsImportable().size(), 2);
		Assert.assertEquals(report.getFieldsImportable().get("import_person").size(), 6);
		Assert.assertTrue(report.getFieldsImportable().get("import_person").contains("firstName"));
		Assert.assertFalse(report.getFieldsImportable().get("import_person").contains("unknownField"));

		// FieldsUnknown
		Assert.assertEquals(report.getFieldsUnknown().size(), 2);
		Assert.assertEquals(report.getFieldsUnknown().get("import_person").size(), 1);
		Assert.assertTrue(report.getFieldsUnknown().get("import_person").contains("unknownField"));
		Assert.assertEquals(report.getFieldsUnknown().get("import_city").size(), 0);

		// FieldsRequired missing
        Assert.assertEquals(report.getFieldsRequired().size(), 2);
        Assert.assertEquals(report.getFieldsRequired().get("import_person").size(), 1);
        Assert.assertTrue(report.getFieldsRequired().get("import_person").contains("birthday"));
        Assert.assertEquals(report.getFieldsRequired().get("import_city").size(), 0);
    }

    @Test
	public void testImportReport() throws IOException, InvalidFormatException, InterruptedException
	{
		// cleanup
		store.drop("import_person");
		store.drop("import_city");
		store.drop("import_country");

		// create test excel
        File f =  new File(getClass().getResource("/example.xlsx").getFile());
		// TODO add good example to repo

		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepositoryByEntityName("attributes"));

		List<Entity> entities = new ArrayList<Entity>();
		for (Entity e : source.getRepositoryByEntityName("attributes"))
		{
			System.out.println(e);
		}

		EmxImportServiceImpl importer = new EmxImportServiceImpl();
		importer.setRepositoryCollection(store);

		for (EntityMetaData em : importer.getEntityMetaData(source).values())
		{
			System.out.println(em);
		}

		// test import
		EntityImportReport report = importer.doImport(source, null);

        // test report
        Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(2));
        Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"),new Integer(3));


        // test state via queries
		for (Entity e : store.getRepositoryByEntityName("import_city"))
		{
			System.out.println(e);
		}

		for (Entity e : store.getRepositoryByEntityName("import_person"))
		{
			System.out.println(e);
		}

		// wait to make sure logger has outputted
		Thread.sleep(1000);

	}
}
