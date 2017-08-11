package org.molgenis.ui;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.ui.style.ThemeFingerprintRegistry;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.molgenis.util.TemplateResourceUtils;
import org.molgenis.web.PluginAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MolgenisInterceptorTest
{
	private ResourceFingerprintRegistry resourceFingerprintRegistry;
	private ThemeFingerprintRegistry themeFingerprintRegistry;
	private TemplateResourceUtils templateResourceUtils;
	private AppSettings appSettings;
	private LanguageService languageService;

	@BeforeMethod
	public void setUp()
	{
		resourceFingerprintRegistry = mock(ResourceFingerprintRegistry.class);
		themeFingerprintRegistry = mock(ThemeFingerprintRegistry.class);
		templateResourceUtils = mock(TemplateResourceUtils.class);
		appSettings = when(mock(AppSettings.class).getLanguageCode()).thenReturn("en").getMock();
		languageService = mock(LanguageService.class);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void MolgenisInterceptor()
	{
		new MolgenisInterceptor(null, null, null, null, null, null);
	}

	@Test
	public void postHandle() throws Exception
	{
		String environment = "development";
		MolgenisInterceptor molgenisInterceptor = new MolgenisInterceptor(resourceFingerprintRegistry,
				themeFingerprintRegistry, templateResourceUtils, appSettings, languageService, environment);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Object handler = mock(Object.class);
		ModelAndView modelAndView = new ModelAndView();
		molgenisInterceptor.postHandle(request, response, handler, modelAndView);

		Map<String, Object> model = modelAndView.getModel();
		assertEquals(model.get(PluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY),
				resourceFingerprintRegistry);
		assertEquals(model.get(PluginAttributes.KEY_APP_SETTINGS), appSettings);
		assertEquals(model.get(PluginAttributes.KEY_ENVIRONMENT), environment);
		assertTrue(model.containsKey(PluginAttributes.KEY_I18N));
	}
}
