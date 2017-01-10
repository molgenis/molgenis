package org.molgenis.dataexplorer;

import com.google.gson.Gson;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.dataexplorer.settings.DataExplorerSettings;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.dataexplorer.controller.DataExplorerController.MOD_ANNOTATORS;
import static org.molgenis.security.core.Permission.WRITEMETA;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.testng.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class DataExplorerControllerTest extends AbstractTestNGSpringContextTests
{
	@Mock
	private DataExplorerSettings dataExplorerSettings;

	@Mock
	private GenomicDataSettings genomicDataSettings;

	@Mock
	private DataService dataService;

	@Mock
	private FreeMarkerConfigurer freemarkerConfigurer;

	@Mock
	private LanguageService languageService;

	@Mock
	private MolgenisPermissionService molgenisPermissionService = mock(MolgenisPermissionService.class);

	@Mock
	private AttributeFactory attributeFactory;

	@Autowired
	private Gson gson;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	private DataExplorerController controller;

	@BeforeMethod
	public void beforeTest()
	{
		initMocks(this);

		when(molgenisPermissionService.hasPermissionOnEntity("yes", WRITEMETA)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnEntity("no", WRITEMETA)).thenReturn(false);

		controller = new DataExplorerController(dataExplorerSettings, genomicDataSettings, dataService,
				molgenisPermissionService, freemarkerConfigurer, gson, languageService, attributeFactory);

		standaloneSetup(controller).setMessageConverters(gsonHttpMessageConverter).build();
	}

	@Test
	public void getAnnotatorModuleSuccess() throws Exception
	{
		assertEquals("view-dataexplorer-mod-" + MOD_ANNOTATORS,
				controller.getModule(MOD_ANNOTATORS, "yes", mock(Model.class)));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void getAnnotatorModuleFail() throws Exception
	{
		controller.getModule(MOD_ANNOTATORS, "no", mock(Model.class));
	}
}
