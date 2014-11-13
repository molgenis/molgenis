package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.AppConfig;
import org.molgenis.data.DataService;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AppConfig.class)
public class EmxMetaDataParserTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	MysqlRepositoryCollection store;

	private MetaDataParser parser = new EmxMetaDataParser();
	private DataService dataService;

	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	MetaDataServiceImpl mysqlMetaDataRepositories;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		mysqlMetaDataRepositories.recreateMetaDataRepositories();
	}

	@Test
	public void testValidationReport() throws IOException, InvalidFormatException, URISyntaxException
	{
		// create test excel
		File f = ResourceUtils.getFile(getClass(), "/example.xlsx");
		ExcelRepositoryCollection source = new ExcelRepositoryCollection(f);

		assertEquals(source.getNumberOfSheets(), 4);
		assertNotNull(source.getRepositoryByEntityName("attributes"));

		EntitiesValidationReport report = parser.validate(dataService, source);
		// test report
		assertTrue(report.valid());
		/*
		 * EntitiesValidationReportImpl [sheetsImportable={import_person=true, import_city=true},
		 * fieldsImportable={import_person=[firstName, lastName, height, active, children, birthplace],
		 * import_city=[name]}, fieldsUnknown={import_person=[], import_city=[]}, fieldsRequired={import_person=[],
		 * import_city=[]}, fieldsAvailable={import_person=[], import_city=[]}, importOrder=[]]
		 */

		EntitiesValidationReport expected = ImmutableEntitiesValidationReport.createNew()
				.addEntity("import_person", true).addAttribute("firstName").addAttribute("lastName")
				.addAttribute("height").addAttribute("active").addAttribute("children").addAttribute("birthplace")
				.addEntity("import_city", true).addAttribute("name");
		assertEquals(report, expected);
	}
}
