package org.molgenis.data.importer;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.elasticsearch.client.Client;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Package;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.semanticsearch.config.SemanticSearchConfig;
import org.molgenis.data.semanticsearch.service.impl.UntypedTagService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests for the entire EMX importer.
 */
@Test
@ContextConfiguration(classes =
{ ImportTestConfig.class, SemanticSearchConfig.class, EmxImportServiceTest.Config.class })
public class EmxImportServiceTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	public static class Config
	{
		@Bean
		public Client client()
		{
			return Mockito.mock(Client.class);
		}

		@Bean
		public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
		{

			EmbeddedElasticSearchServiceFactory result = Mockito.mock(EmbeddedElasticSearchServiceFactory.class);
			Mockito.when(result.getClient()).thenReturn(client());
			return result;
		}

		@Bean
		public TermFrequencyService termFrequencyService()
		{
			return Mockito.mock(TermFrequencyService.class);
		}

		@Bean
		public MolgenisPermissionService molgenisPermissionService()
		{
			return Mockito.mock(org.molgenis.security.core.MolgenisPermissionService.class);
		}

		@Bean
		public LanguageService languageService()
		{
			return Mockito.mock(LanguageService.class);
		}
	}

	@Autowired
	MysqlRepositoryCollection store;

	@Autowired
	DataServiceImpl dataService;

	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	MetaDataServiceImpl metaDataService;

	@Autowired
	UntypedTagService tagService;

	@Autowired
	MolgenisPermissionService molgenisPermissionService;

	@BeforeMethod
	public void beforeMethod()
	{
		if (dataService.hasRepository("import_person"))
		{
			dataService.deleteAll("import_person");
		}
		if (dataService.hasRepository("import_city"))
		{
			dataService.deleteAll("import_city");
		}
		when(molgenisPermissionService.hasPermissionOnEntity(Mockito.anyString(), Mockito.any(Permission.class)))
				.thenReturn(true);
	}

	@Test
	public void testValidationReport() throws IOException, MolgenisInvalidFormatException, URISyntaxException
	{
		// open test source
		File f = ResourceUtils.getFile(getClass(), "/example_invalid.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		// create importer
		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService, molgenisPermissionService), dataService);

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
	public void testImportReport() throws IOException, MolgenisInvalidFormatException, InterruptedException
	{
		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		Assert.assertEquals(source.getNumberOfSheets(), 4);
		Assert.assertNotNull(source.getRepository("attributes"));

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService, molgenisPermissionService), dataService);

		// test import
		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD, Package.DEFAULT_PACKAGE_NAME);

		// Check children
		List<Entity> entitiesWithChildern = StreamSupport
				.stream(dataService.getRepository("import_person").spliterator(), false)
				.filter(e -> e.getEntities("children").iterator().hasNext())
				.collect(Collectors.toCollection(ArrayList::new));
		Assert.assertEquals(new Integer(entitiesWithChildern.size()), new Integer(1));
		Iterator<Entity> children = entitiesWithChildern.get(0).getEntities("children").iterator();
		Assert.assertEquals(children.next().getIdValue(), "john");
		Assert.assertEquals(children.next().getIdValue(), "jane");

		// Check parents
		List<Entity> entitiesWithParents = StreamSupport
				.stream(dataService.getRepository("import_person").spliterator(), false)
				.filter(e -> (e.getEntities("parent").iterator().hasNext()))
				.collect(Collectors.toCollection(ArrayList::new));
		Entity parent1 = entitiesWithParents.get(0).getEntity("parent");
		Assert.assertEquals(parent1.getIdValue().toString(), "john");
		Entity parent2 = entitiesWithParents.get(1).getEntity("parent");
		Assert.assertEquals(parent2.getIdValue().toString(), "jane");
		Entity parent3 = entitiesWithParents.get(2).getEntity("parent");
		Assert.assertEquals(parent3.getIdValue().toString(), "geofrey");

		// test report
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(2));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(3));

	}

	@Test
	public void testImportReportNoMeta() throws IOException, MolgenisInvalidFormatException, InterruptedException
	{

		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService, molgenisPermissionService), dataService);

		// test import
		importer.doImport(source, DatabaseAction.ADD, Package.DEFAULT_PACKAGE_NAME);

		// create test excel
		File file_no_meta = ResourceUtils.getFile(getClass(), "/example_no_meta.xlsx");
		ExcelRepositoryCollection source_no_meta = new ExcelRepositoryCollection(file_no_meta);

		// test import
		EntityImportReport report = importer.doImport(source_no_meta, DatabaseAction.ADD, Package.DEFAULT_PACKAGE_NAME);

		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_city"), new Integer(4));
		Assert.assertEquals(report.getNrImportedEntitiesMap().get("import_person"), new Integer(4));
	}

	@Test
	public void canImportWithAttributes()
	{
		File file = Mockito.mock(File.class);
		when(file.getName()).thenReturn("file.xlsx");

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataService), new ImportWriter(
				dataService, permissionSystemService, tagService, molgenisPermissionService), dataService);

		RepositoryCollection source = Mockito.mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(Arrays.asList("attributes"));

		assertTrue(importer.canImport(file, source));
	}

	@Test
	public void canImportWithoutAttributes()
	{
		// create test excel
		File file = Mockito.mock(File.class);
		when(file.getName()).thenReturn("file.xlsx");

		DataService dataServiceMock = Mockito.mock(DataService.class);
		MetaDataService metaDataServiceMock = Mockito.mock(MetaDataService.class);
		when(dataServiceMock.getMeta()).thenReturn(metaDataServiceMock);
		when(metaDataServiceMock.getEntityMetaData("existingAttribute")).thenReturn(Mockito.mock(EntityMetaData.class));

		EmxImportService importer = new EmxImportService(new EmxMetaDataParser(dataServiceMock), new ImportWriter(
				dataServiceMock, permissionSystemService, tagService, molgenisPermissionService), dataServiceMock);

		RepositoryCollection source = Mockito.mock(RepositoryCollection.class);
		when(source.getEntityNames()).thenReturn(Arrays.asList("existingAttribute"));

		assertTrue(importer.canImport(file, source));
	}
}
