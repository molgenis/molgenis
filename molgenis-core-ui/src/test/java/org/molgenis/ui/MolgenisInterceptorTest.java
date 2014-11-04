package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisInterceptorTest
{
	private ResourceFingerprintRegistry resourceFingerprintRegistry;

	@BeforeMethod
	public void setUp()
	{
		resourceFingerprintRegistry = mock(ResourceFingerprintRegistry.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void MolgenisInterceptor()
	{
		MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
		new MolgenisInterceptor(null, molgenisSettings);
	}

	@Test
	public void postHandle() throws Exception
	{
		MolgenisSettings molgenisSettings = mock(MolgenisSettings.class);
		when(molgenisSettings.getProperty("i18nLocale", "en")).thenReturn("en");
		MolgenisInterceptor molgenisInterceptor = new MolgenisInterceptor(resourceFingerprintRegistry, molgenisSettings);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Object handler = mock(Object.class);
		ModelAndView modelAndView = new ModelAndView();
		molgenisInterceptor.postHandle(request, response, handler, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY),
				resourceFingerprintRegistry);
	}
}
