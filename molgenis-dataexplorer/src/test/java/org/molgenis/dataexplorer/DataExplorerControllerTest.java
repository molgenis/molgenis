package org.molgenis.dataexplorer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.ui.menumanager.MenuManagerService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_CSV;
import static org.molgenis.dataexplorer.controller.DataRequest.DownloadType.DOWNLOAD_TYPE_XLSX;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class DataExplorerControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	@InjectMocks
	private DataExplorerController controller = new DataExplorerController();

	@Mock
	private Repository<Entity> repository;

	@Mock
	private EntityType entityType;
	private String entityTypeId = "id";

	@Mock
	private Entity entity;
	private String entityId = "1";

	@Mock
	private Attribute idAttr;

	@Mock
	private Configuration configuration;

	@Mock
	private Model model;

	@Mock
	public AppSettings appSettings;
	@Mock
	DataExplorerSettings dataExplorerSettings;
	@Mock
	DataService dataService;
	@Mock
	FreeMarkerConfigurer freemarkerConfigurer;
	@Mock
	MenuManagerService menuManager;
	@Mock
	LanguageService languageService;
	@Mock
	MolgenisPermissionService molgenisPermissionService = mock(MolgenisPermissionService.class);
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;
	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeTest() throws IOException
	{
		when(molgenisPermissionService.hasPermissionOnEntity("yes", Permission.WRITEMETA)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnEntity("no", Permission.WRITEMETA)).thenReturn(false);

		when(idAttr.getDataType()).thenReturn(STRING);
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(repository.findOneById(entityId)).thenReturn(entity);
		when(dataService.getEntityType(entityTypeId)).thenReturn(entityType);
		when(dataService.getRepository(entityTypeId)).thenReturn(repository);

		when(dataExplorerSettings.getEntityReport(entityTypeId)).thenReturn("template");
		when(dataExplorerSettings.getModStandaloneReports()).thenReturn(true);

		when(freemarkerConfigurer.getConfiguration()).thenReturn(configuration);

		mockMvc = MockMvcBuilders.standaloneSetup(controller).setMessageConverters(gsonHttpMessageConverter).build();
	}

	@Test
	public void getAnnotatorModuleSuccess() throws Exception
	{
		assertEquals("view-dataexplorer-mod-" + DataExplorerController.MOD_ANNOTATORS,
				controller.getModule(DataExplorerController.MOD_ANNOTATORS, "yes", mock(Model.class)));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void getAnnotatorModuleFail() throws Exception
	{
		controller.getModule(DataExplorerController.MOD_ANNOTATORS, "no", mock(Model.class));
	}

	@Test
	public void testViewEntityDetails() throws Exception
	{
		when(configuration.getTemplate("view-entityreport-specific-template.ftl")).thenReturn(mock(Template.class));

		String actual = controller.viewEntityDetails(entityTypeId, entityId, model);
		String expected = "view-entityreport";

		Assert.assertEquals(actual, expected);
		verify(model).addAttribute("entity", entity);
		verify(model).addAttribute("entityType", entityType);
		verify(model).addAttribute("viewName", "view-entityreport-specific-template");
		verify(model).addAttribute("showStandaloneReportUrl", true);
		verify(model).addAttribute("entityTypeId", entityTypeId);
		verify(model).addAttribute("entityId", entityId);
	}

	@Test
	public void testViewEntityDetailsById() throws Exception
	{
		when(configuration.getTemplate("view-standalone-report-specific-" + entityTypeId + ".ftl")).thenReturn(
				mock(Template.class));

		String actual = controller.viewEntityDetailsById(entityTypeId, entityId, model);
		String expected = "view-standalone-report";

		Assert.assertEquals(actual, expected);
		verify(model).addAttribute("entity", entity);
		verify(model).addAttribute("entityType", entityType);
		verify(model).addAttribute("entityTypeId", entityTypeId);
		verify(model).addAttribute("viewName", "view-standalone-report-specific-id");
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class, expectedExceptionsMessageRegExp = "EntityType with id \\[id\\] does not exist\\. Did you use the correct URL\\?")
	public void testViewEntityDetailsByIdEntityTypeNotExists() throws Exception
	{
		when(dataService.getEntityType(entityTypeId)).thenReturn(null);
		controller.viewEntityDetailsById(entityTypeId, entityId, model);
		verifyNoMoreInteractions(model);
	}

	@Test
	public void testGetDownloadFilenameCsv()
	{
		assertEquals(
				controller.getDownloadFilename("it_emx_datatypes_TypeTest", LocalDateTime.parse("2017-07-04T14:14:33"),
						DOWNLOAD_TYPE_CSV), "it_emx_datatypes_TypeTest_2017-07-04_14_14_33.csv");
	}

	@Test
	public void testGetDownloadFilenameXlsx()
	{
		assertEquals(
				controller.getDownloadFilename("it_emx_datatypes_TypeTest", LocalDateTime.parse("2017-07-04T14:14:33"),
						DOWNLOAD_TYPE_XLSX), "it_emx_datatypes_TypeTest_2017-07-04_14_14_33.xlsx");
	}
}
