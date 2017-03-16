package org.molgenis.dataexplorer;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.i18n.LanguageService;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class DataExplorerControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	@InjectMocks
	private DataExplorerController controller = new DataExplorerController();

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
	public void beforeTest()
	{
		when(molgenisPermissionService.hasPermissionOnEntity("yes", Permission.WRITEMETA)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnEntity("no", Permission.WRITEMETA)).thenReturn(false);

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
}
