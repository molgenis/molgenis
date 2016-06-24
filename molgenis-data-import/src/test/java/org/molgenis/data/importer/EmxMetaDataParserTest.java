package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.importer.MyEntitiesValidationReport.AttributeState;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.util.ResourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Parser specific tests.
 */
@ContextConfiguration(classes = ImportTestConfig.class)
public class EmxMetaDataParserTest extends AbstractTestNGSpringContextTests
{
	private MetaDataParser parser;
	private DataService dataService;
	private MetaDataService metaDataService;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		metaDataService = mock(MetaDataService.class);
		parser = new EmxMetaDataParser(dataService);
		when(metaDataService.getEntityMetaDatas()).thenReturn(ImmutableList.<EntityMetaData> of());
		when(dataService.getMeta()).thenReturn(metaDataService);
	}

	@Test
	public void testValidationReport() throws IOException, MolgenisInvalidFormatException, URISyntaxException
	{
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		assertEquals(source.getNumberOfSheets(), 4);
		assertNotNull(source.getRepository("attributes"));

		EntitiesValidationReport report = parser.validate(source);
		// test report
		assertTrue(report.valid());
		EntitiesValidationReport expected = new MyEntitiesValidationReport().addEntity("import_person", true)
				.addAttribute("firstName").addAttribute("lastName").addAttribute("height").addAttribute("active")
				.addAttribute("children").addAttribute("birthplace").addAttribute("parent")
				.addEntity("import_city", true).addAttribute("name");
		assertEquals(report, expected);
	}

	@Test
	public void testValidationReportInvalid() throws IOException, MolgenisInvalidFormatException, URISyntaxException
	{
		File f = ResourceUtils.getFile(getClass(), "/example_invalid.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		assertEquals(source.getNumberOfSheets(), 6);
		assertNotNull(source.getRepository("attributes"));

		EntitiesValidationReport report = parser.validate(source);
		// test report
		assertFalse(report.valid());
		EntitiesValidationReport expected = new MyEntitiesValidationReport().addEntity("import_person", true)
				.addAttribute("firstName").addAttribute("lastName").addAttribute("height").addAttribute("active")
				.addAttribute("children").addAttribute("birthplace").addAttribute("birthday", AttributeState.REQUIRED)
				.addAttribute("unknownField", AttributeState.UNKNOWN)
				.addAttribute("otherAttribute", AttributeState.AVAILABLE).addEntity("import_city", true)
				.addAttribute("name").addEntity("unknown_entity", false).addEntity("unknown_fields", false);
		assertEquals(report, expected);
	}

	@Test
	public void testValidationReportNoMeta() throws MolgenisInvalidFormatException, IOException
	{
		MysqlRepository repositoryCity = mock(MysqlRepository.class);
		DefaultEntityMetaData entityMetaDataCity = new DefaultEntityMetaData("import_city");
		entityMetaDataCity.addAttribute("name", ROLE_ID);
		when(dataService.getRepository("import_city")).thenReturn(repositoryCity);
		when(repositoryCity.getEntityMetaData()).thenReturn(entityMetaDataCity);

		MysqlRepository repositoryPerson = mock(MysqlRepository.class);
		DefaultEntityMetaData entityMetaDataPerson = new DefaultEntityMetaData("import_person");
		entityMetaDataPerson.addAttribute("firstName", ROLE_ID);
		entityMetaDataPerson.addAttribute("lastName");
		entityMetaDataPerson.addAttribute("height").setDataType(MolgenisFieldTypes.INT);
		entityMetaDataPerson.addAttribute("active").setDataType(MolgenisFieldTypes.BOOL);
		entityMetaDataPerson.addAttribute("children").setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(entityMetaDataPerson);
		entityMetaDataPerson.addAttribute("birthplace").setDataType(MolgenisFieldTypes.XREF)
				.setRefEntity(entityMetaDataCity);
		entityMetaDataPerson.addAttribute("otherAttribute").setNillable(true);

		when(dataService.getRepository("import_person")).thenReturn(repositoryPerson);
		when(repositoryPerson.getEntityMetaData()).thenReturn(entityMetaDataPerson);

		// create test excel
		File file_no_meta = ResourceUtils.getFile(getClass(), "/example_no_meta.xlsx");
		ExcelRepositoryCollection source_no_meta = new ExcelRepositoryCollection(file_no_meta);

		// test import
		EntitiesValidationReport report = parser.validate(source_no_meta);
		EntitiesValidationReport expected = new MyEntitiesValidationReport().addEntity("import_person", true)
				.addAttribute("firstName").addAttribute("lastName").addAttribute("height").addAttribute("active")
				.addAttribute("children").addAttribute("birthplace")
				.addAttribute("otherAttribute", AttributeState.AVAILABLE).addEntity("import_city", true)
				.addAttribute("name");
		assertEquals(report, expected);

	}
}
