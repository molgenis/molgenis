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
import org.molgenis.data.support.GenomicDataSettings;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;
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
	public AppSettings appSettings;
	@Mock
	DataExplorerSettings dataExplorerSettings;
	@Mock
	GenomicDataSettings genomicDataSettings;
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

		when(configuration.getTemplate(entityTypeId + ".ftl")).thenReturn(mock(Template.class));
		when(freemarkerConfigurer.getConfiguration()).thenReturn(configuration);

		when(dataExplorerSettings.getModStandaloneReports()).thenReturn(true);

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
		String actual = controller.viewEntityDetails(entityTypeId, entityId, mock(Model.class));
		String expected = "view-entityreport";

		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testViewEntityDetailsById() throws Exception
	{
		String actual = controller.viewEntityDetailsById(entityTypeId, entityId, mock(Model.class));
		String expected = "view-standalone-report";

		Assert.assertEquals(actual, expected);
	}
}
