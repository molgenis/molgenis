package org.molgenis.ui;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		new MolgenisInterceptor(null);
	}

	@Test
	public void postHandle() throws Exception
	{
		MolgenisInterceptor molgenisInterceptor = new MolgenisInterceptor(resourceFingerprintRegistry);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Object handler = mock(Object.class);
		ModelAndView modelAndView = new ModelAndView();
		molgenisInterceptor.postHandle(request, response, handler, modelAndView);
		assertEquals(modelAndView.getModel().get(MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY),
				resourceFingerprintRegistry);
	}
}
