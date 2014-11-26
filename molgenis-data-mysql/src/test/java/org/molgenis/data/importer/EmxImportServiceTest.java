package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.semantic.UntypedTagService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests for the entire EMX importer.
 */
@ContextConfiguration(classes = AppConfig.class)
public class EmxImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection store;

	DataService dataService;

	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	MetaDataServiceImpl metaDataService;

	@Autowired
	UntypedTagService tagService;

	@BeforeMethod
	public void beforeMethod()
	{
		metaDataService.recreateMetaDataRepositories();
		dataService = mock(DataService.class);
	}

	@Test
	public void testValidationReport() throws IOException, InvalidFormatException, URISyntaxException
	{
		// open test source
		File f = ResourceUtils.getFile(getClass(), "/example_invalid.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		// create importer
		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService, metaDataService),
				new ImportWriter(dataService, metaDataService, permissionSystemService, tagService));
		importer.setRepositoryCollection(store, metaDataService);

		// generate report
		EntitiesValidationReport report = importer.validateImport(f, source);

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
		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepositoryByEntityName("attributes"));

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService, metaDataService),
				new ImportWriter(dataService, metaDataService, permissionSystemService, tagService));
		importer.setRepositoryCollection(store, metaDataService);

		// test import
		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD);

		// test report
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(2));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));

	}

	@Test
	public void testImportReportNoMeta() throws IOException, InvalidFormatException, InterruptedException
	{
		MysqlRepository repositoryCity = mock(MysqlRepository.class);
		DefaultEntityMetaData entityMetaDataCity = new DefaultEntityMetaData("import_city");
		entityMetaDataCity.addAttribute("name").setIdAttribute(true).setNillable(false);
		when(dataService.getRepositoryByEntityName("import_city")).thenReturn(repositoryCity);
		when(repositoryCity.getEntityMetaData()).thenReturn(entityMetaDataCity);

		MysqlRepository repositoryPerson = mock(MysqlRepository.class);
		DefaultEntityMetaData entityMetaDataPerson = new DefaultEntityMetaData("import_person");
		entityMetaDataPerson.addAttribute("firstName").setIdAttribute(true).setNillable(false);
		entityMetaDataPerson.addAttribute("lastName");
		entityMetaDataPerson.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		entityMetaDataPerson.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);
		entityMetaDataPerson.addAttribute("children").setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(entityMetaDataPerson);
		entityMetaDataPerson.addAttribute("birthplace").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(entityMetaDataCity);

		when(dataService.getRepositoryByEntityName("import_person")).thenReturn(repositoryPerson);
		when(repositoryPerson.getEntityMetaData()).thenReturn(entityMetaDataPerson);

		// cleanup
		store.dropEntityMetaData("import_person");
		store.dropEntityMetaData("import_city");
		store.dropEntityMetaData("import_country");

		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService, metaDataService),
				new ImportWriter(dataService, metaDataService, permissionSystemService, tagService));
		importer.setRepositoryCollection(store, metaDataService);

		// test import
		importer.doImport(source, DatabaseAction.ADD);

		// create test excel
		File file_no_meta = ResourceUtils.getFile(getClass(), "/example_no_meta.xlsx");
		ExcelRepositoryCollection source_no_meta = new ExcelRepositoryCollection(file_no_meta);

		// test import
		EntityImportReport report = importer.doImport(source_no_meta, DatabaseAction.ADD);

		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(4));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(4));

	}
}
