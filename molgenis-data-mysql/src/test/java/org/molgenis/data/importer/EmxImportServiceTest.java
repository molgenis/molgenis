package org.molgenis.data.importer;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = AppConfig.class)
public class EmxImportServiceTest extends AbstractTestNGSpringContextTests
{
	private static class SimplePlatformTransactionManager implements PlatformTransactionManager
	{
		@Override
		public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException
		{
			return new SimpleTransactionStatus();
		}

		@Override
		public void commit(TransactionStatus status) throws TransactionException
		{
		}

		@Override
		public void rollback(TransactionStatus status) throws TransactionException
		{
		}
	}

	@Autowired
	MysqlRepositoryCollection store;

	DataService dataService;

	@Test
	public void testValidationReport() throws IOException, InvalidFormatException
	{
		// open test source
		File f = new File(getClass().getResource("/example_invalid.xlsx").getFile());
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);
		dataService = mock(DataService.class);

		// create importer
		EmxImportServiceImpl importer = new EmxImportServiceImpl(dataService);
		importer.setRepositoryCollection(store);
		importer.setPlatformTransactionManager(new SimplePlatformTransactionManager());

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
		File f = new File(getClass().getResource("/example.xlsx").getFile());
		// TODO add good example to repo

		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepositoryByEntityName("attributes"));

		EmxImportServiceImpl importer = new EmxImportServiceImpl(dataService);
		importer.setRepositoryCollection(store);
		importer.setPlatformTransactionManager(new SimplePlatformTransactionManager());

		// test import
		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD);

		// test report
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(2));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));

		// wait to make sure logger has outputted
		Thread.sleep(1000);
	}

	@Test
	public void testImportReportNoMeta() throws IOException, InvalidFormatException, InterruptedException
	{
		dataService = mock(DataService.class);
		MysqlRepository repository = mock(MysqlRepository.class);
		EntityMetaData entityMetaDataPerson = new DefaultEntityMetaData("import_person");
		EntityMetaData entityMetaDataCity = new DefaultEntityMetaData("import_city");
		entityMetaDataPerson.getAttribute("firstName");
		entityMetaDataPerson.getAttribute("lastName");
		entityMetaDataPerson.getAttribute("height");
		entityMetaDataPerson.getAttribute("active");
		entityMetaDataPerson.getAttribute("children");
		entityMetaDataPerson.getAttribute("birthplace");
		entityMetaDataCity.getAttribute("name");
		when(dataService.getRepositoryByEntityName("import_person")).thenReturn(repository);
		when(repository.getEntityMetaData()).thenReturn(entityMetaDataPerson);
		when(dataService.getRepositoryByEntityName("import_city")).thenReturn(repository);
		when(repository.getEntityMetaData()).thenReturn(entityMetaDataCity);

		// cleanup
		store.drop("import_person");
		store.drop("import_city");
		store.drop("import_country");

		// create test excel
		File f = new File(getClass().getResource("/example.xlsx").getFile());
		// TODO add good example to repo

		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		EmxImportServiceImpl importer = new EmxImportServiceImpl(dataService);
		importer.setRepositoryCollection(store);
		importer.setPlatformTransactionManager(new SimplePlatformTransactionManager());

		// test import
		importer.doImport(source, DatabaseAction.ADD);
		// wait to make sure logger has outputted
		Thread.sleep(1000);

		// create test excel
		File file_no_meta = new File(getClass().getResource("/example_no_meta.xlsx").getFile());
		ExcelRepositoryCollection source_no_meta = new ExcelRepositoryCollection(file_no_meta);

		importer.setRepositoryCollection(store);
		importer.setPlatformTransactionManager(new SimplePlatformTransactionManager());

		// test import
		EntityImportReport report = importer.doImport(source_no_meta, DatabaseAction.ADD);

		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(4));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(4));

		// wait to make sure logger has outputted
		Thread.sleep(1000);
	}
}
